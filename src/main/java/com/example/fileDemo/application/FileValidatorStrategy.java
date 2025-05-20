package com.example.fileDemo.application;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileValidatorStrategy {
    void validate(MultipartFile file, String userId) throws IOException;
}
