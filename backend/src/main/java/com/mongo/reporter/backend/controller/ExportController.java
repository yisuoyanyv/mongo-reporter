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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * 批量导出报表
     */
    @PostMapping("/batch")
    public ResponseEntity<byte[]> batchExport(@RequestBody Map<String, Object> batchExportRequest) {
        try {
            @SuppressWarnings("unchecked")
            List<String> reportIds = (List<String>) batchExportRequest.get("reportIds");
            String format = (String) batchExportRequest.getOrDefault("format", "excel");
            @SuppressWarnings("unchecked")
            Map<String, Object> options = (Map<String, Object>) batchExportRequest.getOrDefault("options", new HashMap<>());
            
            if (reportIds == null || reportIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // 获取所有报表
            List<ReportConfig> reports = new ArrayList<>();
            for (String reportId : reportIds) {
                Optional<ReportConfig> reportOpt = reportConfigRepository.findById(reportId);
                if (reportOpt.isPresent()) {
                    reports.add(reportOpt.get());
                }
            }
            
            if (reports.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] exportData;
            String filename;
            String contentType;
            
            switch (format.toLowerCase()) {
                case "excel":
                    exportData = exportToExcel(reports, options);
                    filename = "批量导出_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "pdf":
                    exportData = exportToPdf(reports, options);
                    filename = "批量导出_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";
                    contentType = "application/pdf";
                    break;
                case "json":
                    exportData = exportToJson(reports, options);
                    filename = "批量导出_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".json";
                    contentType = "application/json";
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", contentType)
                .body(exportData);
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
    
    /**
     * 导出为Excel格式
     */
    private byte[] exportToExcel(List<ReportConfig> reports, Map<String, Object> options) throws Exception {
        // 这里应该使用Apache POI或其他Excel库
        // 为了演示，我们返回一个简单的CSV格式
        StringBuilder csv = new StringBuilder();
        csv.append("报表名称,描述,分类,标签,状态,创建时间,更新时间\n");
        
        for (ReportConfig report : reports) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                report.getName() != null ? report.getName() : "",
                report.getDescription() != null ? report.getDescription() : "",
                report.getCategory() != null ? report.getCategory() : "",
                report.getTags() != null ? String.join(";", report.getTags()) : "",
                report.getStatus() != null ? report.getStatus() : "",
                report.getCreatedAt() != null ? report.getCreatedAt() : "",
                report.getUpdatedAt() != null ? report.getUpdatedAt() : ""
            ));
        }
        
        return csv.toString().getBytes("UTF-8");
    }
    
    /**
     * 导出为PDF格式
     */
    private byte[] exportToPdf(List<ReportConfig> reports, Map<String, Object> options) throws Exception {
        // 这里应该使用iText或其他PDF库
        // 为了演示，我们返回一个简单的文本格式
        StringBuilder pdf = new StringBuilder();
        pdf.append("MongoReporter 批量导出报告\n");
        pdf.append("导出时间: ").append(new java.util.Date()).append("\n\n");
        
        for (ReportConfig report : reports) {
            pdf.append("报表名称: ").append(report.getName()).append("\n");
            pdf.append("描述: ").append(report.getDescription()).append("\n");
            pdf.append("分类: ").append(report.getCategory()).append("\n");
            pdf.append("状态: ").append(report.getStatus()).append("\n");
            pdf.append("创建时间: ").append(report.getCreatedAt()).append("\n");
            pdf.append("---\n");
        }
        
        return pdf.toString().getBytes("UTF-8");
    }
    
    /**
     * 导出为JSON格式
     */
    private byte[] exportToJson(List<ReportConfig> reports, Map<String, Object> options) throws Exception {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("exportTime", new java.util.Date());
        exportData.put("totalCount", reports.size());
        exportData.put("reports", reports);
        
        // 使用Jackson或其他JSON库
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(exportData);
    }
} 