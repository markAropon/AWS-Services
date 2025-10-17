package com.srllc.AmazonServices.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RekognitionResponse {

    private List<Celebrity> celebrities;
    private int unrecognizedFaces;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Celebrity {
        private String name;
        private String id;
        private Float confidence;
        private List<String> urls;
        private BoundingBox boundingBox;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private Float width;
        private Float height;
        private Float left;
        private Float top;
    }
}