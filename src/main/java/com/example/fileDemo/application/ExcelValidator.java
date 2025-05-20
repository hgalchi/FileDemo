package com.example.fileDemo.application;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component("xlsx")
public class ExcelValidator implements FileValidatorStrategy{
    @Override
    public void validate(MultipartFile file, String userId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            if (rowCount == 1) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }

            Row firstRow = sheet.getRow(0);
            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("사용자 ID가 비어 있습니다.");
            }

            String fileUserId = getCellValue(firstRow.getCell(0));
            if (!fileUserId.equals(userId)) {
                throw new IllegalArgumentException("사용자 ID가 일치하지 않습니다.");
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
}
