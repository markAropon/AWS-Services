package com.srllc.AmazonServices.domain.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.domain.entity.RecieptItem;
import com.srllc.AmazonServices.domain.entity.Reciepts;
import com.srllc.AmazonServices.domain.exception.BadRequestException;
import com.srllc.AmazonServices.domain.exception.ResourceNotFoundException;
import com.srllc.AmazonServices.domain.exception.TextractException;
import com.srllc.AmazonServices.domain.repository.RecieptsRepository;
import com.srllc.AmazonServices.domain.service.TextractServiceInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;

@Service
@Slf4j
@RequiredArgsConstructor
public class TextractServiceImpl implements TextractServiceInterface {
    private final TextractClient textractClient;
    private final RecieptsRepository receiptsRepository;

    @Override
    public Reciepts extractReceiptData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be null or empty");
        }

        try {
            log.info("Extracting receipt data from image using Amazon Textract");
            byte[] imageBytes = file.getBytes();

            var request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .build();

            var response = textractClient.detectDocumentText(request);
            log.info("Textract returned {} blocks", response.blocks().size());

            List<String> lines = response.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(Block::text)
                    .toList();

            log.info("All extracted lines:");
            for (int i = 0; i < lines.size(); i++) {
                log.info("Line {}: '{}'", i + 1, lines.get(i));
            }

            Reciepts receipt = parseReceiptData(lines);
            return receiptsRepository.save(receipt);

        } catch (IOException e) {
            log.error("Error reading file bytes: {}", e.getMessage());
            throw new BadRequestException("Invalid file format");
        } catch (Exception e) {
            log.error("Error extracting text from image: {}", e.getMessage());
            throw new TextractException("Failed to extract receipt data: " + e.getMessage());
        }
    }

    @Override
    public Reciepts getReceiptById(Long id) {
        return receiptsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id: " + id));
    }

    private Reciepts parseReceiptData(List<String> lines) {
        Reciepts receipt = new Reciepts();
        List<RecieptItem> items = new ArrayList<>();

        String companyName = null;
        String branch = null;
        String managerName = null;
        String cashierNumber = null;
        Double subTotal = null;
        Double cash = null;
        Double change = null;

        boolean expectingManagerName = false;
        boolean expectingCashierNumber = false;
        boolean expectingSubTotal = false;
        boolean expectingCash = false;
        boolean expectingChange = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            log.info("Processing line: {}", line);

            // Skip lines with unwanted texts
            if (line.toLowerCase().contains("index") || line.toLowerCase().contains("confidence") ||
                    line.toLowerCase().contains("coord") || line.length() > 50) {
                continue;
            }

            // Handle expected values from previous lines
            if (expectingManagerName && managerName == null) {
                managerName = line;
                log.info("Extracted manager from next line: {}", managerName);
                expectingManagerName = false;
                continue;
            }

            if (expectingCashierNumber && cashierNumber == null) {
                cashierNumber = line;
                log.info("Extracted cashier from next line: {}", cashierNumber);
                expectingCashierNumber = false;
                continue;
            }

            if (expectingSubTotal && subTotal == null) {
                Double amount = extractAmount(line);
                if (amount != null) {
                    subTotal = amount;
                    log.info("Extracted subtotal from next line: {}", subTotal);
                }
                expectingSubTotal = false;
                continue;
            }

            if (expectingCash && cash == null) {
                Double amount = extractAmount(line);
                if (amount != null) {
                    cash = amount;
                    log.info("Extracted cash from next line: {}", cash);
                }
                expectingCash = false;
                continue;
            }

            if (expectingChange && change == null) {
                Double amount = extractAmount(line);
                if (amount != null) {
                    change = amount;
                    log.info("Extracted change from next line: {}", change);
                }
                expectingChange = false;
                continue;
            }

            // Parse current line
            if ((line.toUpperCase().contains("HYPERMARKET") || line.toUpperCase().contains("STORE") ||
                    line.toUpperCase().contains("MALL") || line.toUpperCase().contains("MARKET"))
                    && companyName == null) {
                companyName = line;
                log.info("Extracted company name: {}", companyName);
            }

            else if ((line.toLowerCase().contains("branch") || line.toLowerCase().contains("quezon") ||
                    line.toLowerCase().contains("manila") || line.toLowerCase().contains("city")) &&
                    !line.toLowerCase().contains("index") && branch == null) {
                branch = line;
                log.info("Extracted branch: {}", branch);
            }

            else if (line.toLowerCase().equals("manager:") && managerName == null) {
                log.info("Found manager label, expecting name on next line");
                expectingManagerName = true;
            }

            else if (line.toLowerCase().equals("cashier:") && cashierNumber == null) {
                log.info("Found cashier label, expecting number on next line");
                expectingCashierNumber = true;
            }

            else if ((line.toLowerCase().equals("sub total") || line.toLowerCase().equals("subtotal"))
                    && subTotal == null) {
                log.info("Found subtotal label, expecting amount on next line");
                expectingSubTotal = true;
            }

            else if (line.toLowerCase().equals("cash") && cash == null) {
                log.info("Found cash label, expecting amount on next line");
                expectingCash = true;
            }

            else if (line.toLowerCase().equals("change") && change == null) {
                log.info("Found change label, expecting amount on next line");
                expectingChange = true;
            }

            // Try to extract items using a different approach for this receipt format
            else if (isProductName(line) && i + 2 < lines.size()) {
                // Check if next two lines are quantity and price
                String nextLine = lines.get(i + 1).trim();
                String priceLine = lines.get(i + 2).trim();

                if (isQuantity(nextLine) && isPrice(priceLine)) {
                    RecieptItem item = new RecieptItem();
                    item.setProductName(line);
                    item.setQuantity(Integer.parseInt(nextLine));
                    item.setPrice(extractAmount(priceLine));
                    item.setReciepts(receipt);
                    items.add(item);

                    log.info("Parsed item: {} - Qty: {} - Price: {}",
                            item.getProductName(), item.getQuantity(), item.getPrice());

                    // Skip the next two lines as we've processed them
                    i += 2;
                }
            }
        }

        log.info("Parsing complete. Summary:");
        log.info("Company: {}, Branch: {}, Manager: {}, Cashier: {}", companyName, branch, managerName, cashierNumber);
        log.info("Amounts - SubTotal: {}, Cash: {}, Change: {}", subTotal, cash, change);
        log.info("Items found: {}", items.size());

        receipt.setCompanyName(companyName);
        receipt.setBranch(branch);
        receipt.setManagerName(managerName);
        receipt.setCashierNumber(cashierNumber);
        receipt.setSubTotal(subTotal);
        receipt.setCash(cash);
        receipt.setChange(change);
        receipt.setItems(items);

        return receipt;
    }

    private boolean isProductName(String line) {
        // Check if line looks like a product name
        return line.length() > 1 &&
                line.matches("^[A-Za-z].*") &&
                !line.toLowerCase().contains("name") &&
                !line.toLowerCase().contains("qty") &&
                !line.toLowerCase().contains("price") &&
                !line.toLowerCase().contains("total") &&
                !line.toLowerCase().contains("cash") &&
                !line.toLowerCase().contains("change") &&
                !line.toLowerCase().contains("manager") &&
                !line.toLowerCase().contains("cashier") &&
                !line.toLowerCase().contains("tel") &&
                !line.toLowerCase().contains("thank");
    }

    private boolean isQuantity(String line) {
        return line.matches("^\\d+$");
    }

    private boolean isPrice(String line) {
        return line.matches("^\\$\\d+\\.\\d{2}$");
    }

    private String extractManagerName(String line) {
        // Handle "Manager: Eric Steer" format
        if (line.toLowerCase().contains("manager:")) {
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }

        // Fallback pattern for names
        Pattern pattern = Pattern.compile("([A-Za-z]+\\s+[A-Za-z]+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Double extractAmount(String line) {
        // Look for amounts with dollar sign first, then without
        Pattern patternWithDollar = Pattern.compile("\\$(\\d+\\.\\d{2})");
        Matcher matcherWithDollar = patternWithDollar.matcher(line);
        if (matcherWithDollar.find()) {
            return Double.parseDouble(matcherWithDollar.group(1));
        }

        // Fallback to amount without dollar sign
        Pattern pattern = Pattern.compile("(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return null;
    }

    private boolean isItemLine(String line) {
        // More flexible item detection
        boolean hasPrice = line.matches(".*\\$\\d+\\.\\d{2}.*");
        boolean isNotHeader = !line.toLowerCase().contains("total") &&
                !line.toLowerCase().contains("cash") &&
                !line.toLowerCase().contains("change") &&
                !line.toLowerCase().contains("index") &&
                !line.toLowerCase().contains("qty") &&
                !line.toLowerCase().contains("price") &&
                !line.toLowerCase().contains("name") &&
                !line.toLowerCase().contains("manager") &&
                !line.toLowerCase().contains("cashier") &&
                !line.toLowerCase().contains("tel:");

        // Check if it looks like a product name (starts with letters)
        boolean startsWithProduct = line.trim().matches("^[A-Za-z].*");

        return hasPrice && isNotHeader && startsWithProduct && line.length() > 5;
    }

    private RecieptItem parseItemLine(String line) {
        log.info("Attempting to parse item line: '{}'", line);

        Pattern pattern1 = Pattern.compile("([A-Za-z][A-Za-z\\s]+?)\\s+(\\d+)\\s+\\$(\\d+\\.\\d{2})");
        Matcher matcher1 = pattern1.matcher(line);

        if (matcher1.find()) {
            RecieptItem item = new RecieptItem();
            item.setProductName(matcher1.group(1).trim());
            item.setQuantity(Integer.parseInt(matcher1.group(2)));
            item.setPrice(Double.parseDouble(matcher1.group(3)));
            log.info("Parsed with pattern 1: {} - Qty: {} - Price: {}",
                    item.getProductName(), item.getQuantity(), item.getPrice());
            return item;
        }

        // Pattern 2: Just ProductName [spaces] $price (assuming quantity 1)
        Pattern pattern2 = Pattern.compile("([A-Za-z][A-Za-z\\s]+?)\\s+\\$(\\d+\\.\\d{2})");
        Matcher matcher2 = pattern2.matcher(line);

        if (matcher2.find()) {
            RecieptItem item = new RecieptItem();
            item.setProductName(matcher2.group(1).trim());
            item.setQuantity(1);
            item.setPrice(Double.parseDouble(matcher2.group(2)));
            log.info("Parsed with pattern 2: {} - Qty: {} - Price: {}",
                    item.getProductName(), item.getQuantity(), item.getPrice());
            return item;
        }

        // Pattern 3: More flexible - any text followed by digits and price
        Pattern pattern3 = Pattern.compile("(.+?)\\s+(\\d+)\\s+\\$(\\d+\\.\\d{2})");
        Matcher matcher3 = pattern3.matcher(line);

        if (matcher3.find()) {
            String productName = matcher3.group(1).trim();
            // Make sure it's not a header or unwanted text
            if (productName.length() > 2 && !productName.toLowerCase().contains("qty") &&
                    !productName.toLowerCase().contains("price")) {
                RecieptItem item = new RecieptItem();
                item.setProductName(productName);
                item.setQuantity(Integer.parseInt(matcher3.group(2)));
                item.setPrice(Double.parseDouble(matcher3.group(3)));
                log.info("Parsed with pattern 3: {} - Qty: {} - Price: {}",
                        item.getProductName(), item.getQuantity(), item.getPrice());
                return item;
            }
        }

        log.info("Could not parse item from line: '{}'", line);
        return null;
    }
}
