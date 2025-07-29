package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.ReportConfig;
import com.mongo.reporter.backend.repository.ReportConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    /**
     * 导出报表为PDF
     */
    @PostMapping("/pdf/{reportId}")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable String reportId, 
                                            @RequestBody Map<String, Object> exportOptions) {
        try {
            Optional<ReportConfig> reportOpt = reportConfigRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportConfig report = reportOpt.get();
            byte[] pdfContent = generatePdfContent(report, exportOptions);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", report.getName() + ".pdf");
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出报表为Excel
     */
    @PostMapping("/excel/{reportId}")
    public ResponseEntity<byte[]> exportToExcel(@PathVariable String reportId,
                                              @RequestBody Map<String, Object> exportOptions) {
        try {
            Optional<ReportConfig> reportOpt = reportConfigRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportConfig report = reportOpt.get();
            byte[] excelContent = generateExcelContent(report, exportOptions);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", report.getName() + ".xlsx");
            
            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出图表为图片
     */
    @PostMapping("/image/{reportId}")
    public ResponseEntity<byte[]> exportToImage(@PathVariable String reportId,
                                              @RequestBody Map<String, Object> exportOptions) {
        try {
            Optional<ReportConfig> reportOpt = reportConfigRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportConfig report = reportOpt.get();
            String format = (String) exportOptions.getOrDefault("format", "png");
            byte[] imageContent = generateImageContent(report, exportOptions, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("image/" + format));
            headers.setContentDispositionFormData("attachment", report.getName() + "." + format);
            
            return new ResponseEntity<>(imageContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出报表为JSON
     */
    @PostMapping("/json/{reportId}")
    public ResponseEntity<byte[]> exportToJson(@PathVariable String reportId,
                                             @RequestBody Map<String, Object> exportOptions) {
        try {
            Optional<ReportConfig> reportOpt = reportConfigRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportConfig report = reportOpt.get();
            byte[] jsonContent = generateJsonContent(report, exportOptions);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", report.getName() + ".json");
            
            return new ResponseEntity<>(jsonContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取导出选项
     */
    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getExportOptions() {
        Map<String, Object> options = new HashMap<>();
        
        // PDF选项
        Map<String, Object> pdfOptions = new HashMap<>();
        pdfOptions.put("pageSize", "A4");
        pdfOptions.put("orientation", "portrait");
        pdfOptions.put("includeCharts", true);
        pdfOptions.put("includeData", true);
        options.put("pdf", pdfOptions);
        
        // Excel选项
        Map<String, Object> excelOptions = new HashMap<>();
        excelOptions.put("includeCharts", true);
        excelOptions.put("includeData", true);
        excelOptions.put("multipleSheets", true);
        options.put("excel", excelOptions);
        
        // 图片选项
        Map<String, Object> imageOptions = new HashMap<>();
        imageOptions.put("format", "png");
        imageOptions.put("width", 800);
        imageOptions.put("height", 600);
        imageOptions.put("quality", 0.9);
        options.put("image", imageOptions);
        
        return ResponseEntity.ok(options);
    }

    // 私有方法：生成PDF内容
    private byte[] generatePdfContent(ReportConfig report, Map<String, Object> options) throws IOException {
        // 这里应该实现PDF生成逻辑
        // 暂时返回模拟数据
        String content = "PDF Export for: " + report.getName() + "\n";
        content += "Description: " + report.getDescription() + "\n";
        content += "Created: " + report.getCreatedAt() + "\n";
        
        return content.getBytes("UTF-8");
    }

    // 私有方法：生成Excel内容
    private byte[] generateExcelContent(ReportConfig report, Map<String, Object> options) throws IOException {
        // 这里应该实现Excel生成逻辑
        // 暂时返回模拟数据
        String content = "Excel Export for: " + report.getName() + "\n";
        content += "Description: " + report.getDescription() + "\n";
        content += "Created: " + report.getCreatedAt() + "\n";
        
        return content.getBytes("UTF-8");
    }

    // 私有方法：生成图片内容
    private byte[] generateImageContent(ReportConfig report, Map<String, Object> options, String format) throws IOException {
        // 这里应该实现图片生成逻辑
        // 暂时返回模拟数据
        String content = "Image Export for: " + report.getName() + "\n";
        content += "Format: " + format + "\n";
        content += "Description: " + report.getDescription() + "\n";
        
        return content.getBytes("UTF-8");
    }

    // 私有方法：生成JSON内容
    private byte[] generateJsonContent(ReportConfig report, Map<String, Object> options) throws IOException {
        // 这里应该实现JSON生成逻辑
        String content = "{\n";
        content += "  \"name\": \"" + report.getName() + "\",\n";
        content += "  \"description\": \"" + report.getDescription() + "\",\n";
        content += "  \"createdAt\": \"" + report.getCreatedAt() + "\",\n";
        content += "  \"category\": \"" + report.getCategory() + "\",\n";
        content += "  \"version\": \"" + report.getVersion() + "\",\n";
        content += "  \"status\": \"" + report.getStatus() + "\"\n";
        content += "}";
        
        return content.getBytes("UTF-8");
    }
} 