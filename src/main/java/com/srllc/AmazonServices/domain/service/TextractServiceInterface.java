package com.srllc.AmazonServices.domain.service;

import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.domain.entity.Reciepts;

public interface TextractServiceInterface {
    Reciepts extractReceiptData(MultipartFile file);

    Reciepts getReceiptById(Long id);
}
