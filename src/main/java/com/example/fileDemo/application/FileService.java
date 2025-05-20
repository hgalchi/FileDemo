package com.example.fileDemo.application;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class FileService {
    private static Set<String> ALLOWED = Set.of("csv", "xlsx");

    public void FileUpload(MultipartFile file,String userId) throws IllegalAccessException {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        //유효성 검증
        try {
            validateExtension(extension);
            validateFileContent(file, extension, userId);
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException("파일 유효성 검증 실패"+e.getMessage());
        }
        //TODO : s3 파일 전송
        //TODO : db 저장

    }

    private void validateExtension(String extension) throws IllegalAccessException {
        if(extension==null||!ALLOWED.contains(extension)){
            throw new IllegalArgumentException("허용되지 않은 확장자");
        }
    }

    private void validateFileContent(MultipartFile file,String extension,String userId) throws IOException {
        if (extension.equals("csv")) {
            validateCsv(file, userId);
        } else {
            validateExcel(file, userId);
        }

    }

    private void validateCsv(MultipartFile file, String userId) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV가 비어 있습니다.");
            }

            String[] headers = headerLine.split(",");
            String fileUserId = headers[0];
            if (!fileUserId.equals(userId)) {
                throw new IllegalArgumentException("CSV의 사용자 ID가 일치하지 않습니다.");
            }

            int rowCount = 0;
            while (reader.readLine() != null) {
                rowCount++;
            }

            if (rowCount == 1) {
                throw new IllegalArgumentException("CSV파일이 비어 있습니다.");
            }
        }
    }

    private void validateExcel(MultipartFile file, String userId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            if (rowCount == 1) {
                throw new IllegalArgumentException("엑셀 파일이 비어 있습니다.");
            }

            Row firstRow = sheet.getRow(0);
            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("첫 번째 셀에 사용자 ID가 없습니다.");
            }

            String fileUserId = getCellValue(firstRow.getCell(0));
            if (!fileUserId.equals(userId)) {
                throw new IllegalArgumentException("엑셀의 사용자 ID가 일치하지 않습니다."+"fileUserId: "+fileUserId+"- userId:"+userId);
            }
        }
    }

    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public void FileDownload(){

    }
}
