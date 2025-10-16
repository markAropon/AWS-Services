package com.srllc.AmazonServices.common;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private HttpStatus httpStatus;
    private boolean success;
    private String message;
    private T payload;
    private List<T> errors;
    private int errorCode;
    private LocalDateTime timestamp;
    private String path;

}