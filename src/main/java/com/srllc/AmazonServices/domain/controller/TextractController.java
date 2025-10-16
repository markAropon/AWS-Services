package com.srllc.AmazonServices.domain.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.common.ApiResponse;
import com.srllc.AmazonServices.domain.entity.Reciepts;
import com.srllc.AmazonServices.domain.service.TextractServiceInterface;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/textract")
@RequiredArgsConstructor
@Tag(name = "AWS Textract Controller", description = "API for extracting text from receipt images and managing receipt data")
public class TextractController {
    private final TextractServiceInterface textractService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract Receipt Data", description = "Extract structured data from receipt image using Amazon Textract")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Receipt data extracted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input file"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Reciepts>> extractReceiptData(
            @Parameter(description = "Receipt image file to extract data from", required = true) @RequestPart("file") MultipartFile file) {

        Reciepts receipt = textractService.extractReceiptData(file);

        ApiResponse<Reciepts> response = new ApiResponse<>();
        response.setHttpStatus(HttpStatus.OK);
        response.setSuccess(true);
        response.setMessage("Receipt data extracted and saved successfully");
        response.setPayload(receipt);
        response.setErrorCode(200);
        response.setTimestamp(LocalDateTime.now());
        response.setPath("/api/v1/textract/extract");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/{id}")
    @Operation(summary = "Get Receipt by ID", description = "Retrieve a specific receipt by its ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Receipt found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<ApiResponse<Reciepts>> getReceiptById(
            @Parameter(description = "Receipt ID", required = true) @PathVariable Long id) {

        Reciepts receipt = textractService.getReceiptById(id);

        ApiResponse<Reciepts> response = new ApiResponse<>();
        response.setHttpStatus(HttpStatus.OK);
        response.setSuccess(true);
        response.setMessage("Receipt retrieved successfully");
        response.setPayload(receipt);
        response.setErrorCode(200);
        response.setTimestamp(LocalDateTime.now());
        response.setPath("/api/v1/textract/receipts/" + id);

        return ResponseEntity.ok(response);
    }
}
