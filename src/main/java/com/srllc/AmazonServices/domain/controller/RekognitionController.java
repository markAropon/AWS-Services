package com.srllc.AmazonServices.domain.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.domain.service.RekognitionService;
import com.srllc.AmazonServices.dto.RekognitionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rekognition")
@RequiredArgsConstructor
@Tag(name = "Rekognition", description = "Celebrity Recognition API")
public class RekognitionController {

    private final RekognitionService rekognitionService;

    @PostMapping(value = "/celebrities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Recognize celebrities in image")
    public ResponseEntity<RekognitionResponse> recognizeCelebrities(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RekognitionResponse response = rekognitionService.recognizeCelebrities(file);
        return ResponseEntity.ok(response);
    }
}
