package com.legacy.report.controller;

import com.legacy.report.model.Report;
import com.legacy.report.service.ReportExcelExportService;
import com.legacy.report.service.ReportRunService;
import com.legacy.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRunService reportRunService;

    @Autowired
    private ReportExcelExportService reportExcelExportService;
    
    // Test endpoint to verify basic functionality
    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "ok", "message", "Backend is working");
    }
    
    // Test database connectivity
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        try {
            System.out.println("Testing database connection...");
            List<Report> reports = reportService.getAllReports();
            System.out.println("Database test successful, found " + reports.size() + " reports");
            return Map.of("status", "ok", "message", "Database connection working", "count", reports.size());
        } catch (Exception e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
            return Map.of("status", "error", "message", "Database connection failed: " + e.getMessage());
        }
    }
    
    // 没有统一的返回格式
    @GetMapping(value = "/reports", produces = "application/json;charset=UTF-8")
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }
    
    @GetMapping("/reports/{id}")
    public Report getReport(@PathVariable Long id) {
        return reportService.getReportById(id);
    }
    
    // 直接暴露SQL执行接口，这是严重的安全问题
    @PostMapping("/reports/run")
    public Map<String, Object> runReport(@RequestBody Map<String, String> request) {
        throw new RuntimeException("已禁用任意 SQL 执行接口，请使用保存的报表定义与参数化执行接口");
    }
    
    @PostMapping("/reports/generate")
    public Map<String, Object> generateReport(@RequestBody Map<String, Object> request) {
        Long reportId = Long.valueOf(request.get("reportId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> params = request.get("params") instanceof Map
                ? (Map<String, Object>) request.get("params")
                : Map.of();
        return reportService.generateReport(reportId, params);
    }
    
    @PostMapping("/reports")
    public void createReport(@RequestBody Report report) {
        reportService.createReport(report);
    }
    
    @PostMapping("/reports/{id}/execute")
    public List<Map<String, Object>> executeReport(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> request) {
        // 通过 ReportRunService 执行报表并创建 ReportRun + 审计事件，
        // 对前端返回值保持不变：仍然返回查询结果列表。
        Map<String, Object> parameters = request == null || !(request.get("params") instanceof Map)
                ? Map.of()
                : (Map<String, Object>) request.get("params");
        return reportRunService.executeReportWithRun(id, parameters);
    }
    
    @GetMapping("/reports/{id}/export")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id) {
        byte[] body = reportExcelExportService.exportLatestByReportId(id);

        String filename = URLEncoder.encode("report-" + id + ".xlsx", StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename);

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
    
    // 没有更新和删除的接口
}