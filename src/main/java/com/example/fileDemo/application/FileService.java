package com.example.fileDemo.application;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.fileDemo.infrastructor.s3Loader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileValidator validator;
    private final s3Loader s3Loader;

    public void FileUpload(MultipartFile file, String userId) throws IllegalAccessException, IOException {
        String originalFilename = file.getOriginalFilename();
        String s3Filename = createFilename(originalFilename);
        String extension = StringUtils.getFilenameExtension(originalFilename);
        //유효성 검증
        validator.getFileValidator(extension).validate(file,userId);
        //S3 파일 저장
        String url=s3Loader.upload(file, s3Filename);
        //TODO : db 파일 경로 저장

    }
    private String createFilename(String originalFilename) {
        return UUID.randomUUID().toString();
    }



}
