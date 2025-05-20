package com.example.fileDemo.application;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component("csv")
public class CsvValidator implements FileValidatorStrategy{
    @Override
    public void validate(MultipartFile file, String userId) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("사용자 ID가 비어 있습니다.");
            }

            String[] headers = headerLine.split(",");
            String fileUserId = headers[0];
            if (!fileUserId.equals(userId)) {
                throw new IllegalArgumentException("사용자 ID가 일치하지 않습니다.");
            }

            int rowCount = 0;
            while (reader.readLine() != null) {
                rowCount++;
            }

            if (rowCount == 1) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }
        }
    }
}
