package com.srllc.AmazonServices.domain.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.dto.RekognitionResponse;

public interface RekognitionService {

    RekognitionResponse recognizeCelebrities(MultipartFile file) throws IOException;
}