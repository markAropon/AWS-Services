package com.srllc.AmazonServices.domain.service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.srllc.AmazonServices.domain.exception.RekognitionException;
import com.srllc.AmazonServices.domain.service.RekognitionService;
import com.srllc.AmazonServices.dto.RekognitionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Celebrity;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RecognizeCelebritiesRequest;
import software.amazon.awssdk.services.rekognition.model.RecognizeCelebritiesResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class RekognitionServiceImpl implements RekognitionService {

    private final RekognitionClient rekognitionClient;

    @Override
    public RekognitionResponse recognizeCelebrities(MultipartFile file) throws IOException {
        log.info("Recognizing celebrities from uploaded file");

        try {
            SdkBytes imageBytes = SdkBytes.fromInputStream(file.getInputStream());

            Image image = Image.builder()
                    .bytes(imageBytes)
                    .build();

            RecognizeCelebritiesRequest request = RecognizeCelebritiesRequest.builder()
                    .image(image)
                    .build();

            RecognizeCelebritiesResponse response = rekognitionClient.recognizeCelebrities(request);

            return buildResponse(response);

        } catch (software.amazon.awssdk.services.rekognition.model.RekognitionException e) {
            log.error("Rekognition error: {}", e.getMessage());
            throw new RekognitionException(e.awsErrorDetails().errorMessage());
        }
    }

    private RekognitionResponse buildResponse(RecognizeCelebritiesResponse awsResponse) {
        List<RekognitionResponse.Celebrity> celebrities = awsResponse.celebrityFaces().stream()
                .map(this::mapCelebrity)
                .collect(Collectors.toList());

        return RekognitionResponse.builder()
                .celebrities(celebrities)
                .unrecognizedFaces(awsResponse.unrecognizedFaces().size())
                .build();
    }

    private RekognitionResponse.Celebrity mapCelebrity(Celebrity celebrity) {
        return RekognitionResponse.Celebrity.builder()
                .name(celebrity.name())
                .id(celebrity.id())
                .confidence(celebrity.matchConfidence())
                .urls(celebrity.urls())
                .boundingBox(mapBoundingBox(celebrity.face() != null ? celebrity.face().boundingBox() : null))
                .build();
    }

    private RekognitionResponse.BoundingBox mapBoundingBox(
            software.amazon.awssdk.services.rekognition.model.BoundingBox awsBoundingBox) {
        if (awsBoundingBox == null) {
            return null;
        }

        return RekognitionResponse.BoundingBox.builder()
                .width(awsBoundingBox.width())
                .height(awsBoundingBox.height())
                .left(awsBoundingBox.left())
                .top(awsBoundingBox.top())
                .build();
    }
}