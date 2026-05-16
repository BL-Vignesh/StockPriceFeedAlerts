package com.portfolio.stockpricefeed;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class to generate a sample Excel file (sample_portfolio.xlsx)
 * formatted correctly for the saveBulkFromExcel API endpoint.
 *
 * Expected Columns (in order):
 * 0: Stock Symbol (String)
 * 1: Quantity (Numeric)
 * 2: Buy Price (Numeric)
 * 3: Upper Limit (Numeric)
 * 4: Lower Limit (Numeric)
 */
public class GenerateExcel {

    public static void main(String[] args) {
        String filePath = "sample_portfolio.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Portfolio");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Stock Symbol", "Quantity", "Buy Price", "Upper Limit", "Lower Limit"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample Data Rows (Must match valid stocks in MarketDataService.NSE_TICKERS)
            Object[][] sampleData = {
                    {"RELIANCE", 50, 2500.0, 2800.0, 2400.0},
                    {"TCS", 20, 3500.0, 3800.0, 3200.0},
                    {"INFY", 100, 1400.0, 1600.0, 1300.0}
            };

            int rowNum = 1;
            for (Object[] rowData : sampleData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) rowData[0]);
                row.createCell(1).setCellValue(((Number) rowData[1]).doubleValue());
                row.createCell(2).setCellValue(((Number) rowData[2]).doubleValue());
                row.createCell(3).setCellValue(((Number) rowData[3]).doubleValue());
                row.createCell(4).setCellValue(((Number) rowData[4]).doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                System.out.println("Successfully generated Excel file at: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("Error generating Excel file: " + e.getMessage());
        }
    }
}
