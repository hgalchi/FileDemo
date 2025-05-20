package com.example.fileDemo.application;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileValidator {
    private final Map<String,FileValidatorStrategy> fileValidatorMap;

    public FileValidator(Map<String,FileValidatorStrategy> fileValidatorMap){
        this.fileValidatorMap = fileValidatorMap;
    }

    public FileValidatorStrategy getFileValidator(String extension) {
        if(!fileValidatorMap.containsKey(extension)){
            throw new IllegalArgumentException("지원하지 않은 확장자입니다.");}
        return fileValidatorMap.get(extension);
    }
}
