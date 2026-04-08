package com.legacy.report.service;

import com.legacy.report.dao.ReportDao;
import com.legacy.report.model.Report;
import com.legacy.report.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ReportService {

    private static final Pattern FORBIDDEN_SQL_PATTERN = Pattern.compile("(?i)\\b(insert|update|delete|drop|alter|truncate|merge|call|execute|exec|grant|revoke|create)\\b");

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private CurrentUserService currentUserService;

    public List<Report> getAllReports() {
        User currentUser = getCurrentUserSafely();
        if (currentUser == null) {
            return reportDao.findAll();
        }
        Set<Long> allowedReportIds = currentUserService.getAllowedReportIds(currentUser);
        if (allowedReportIds.isEmpty()) {
            return reportDao.findAll();
        }
        return reportDao.findAllByIds(allowedReportIds);
    }

    public Report getReportById(Long id) {
        Report report = reportDao.findById(id);
        if (report == null) {
            return null;
        }
        User currentUser = getCurrentUserSafely();
        if (currentUser != null) {
            currentUserService.requireReportAccess(currentUser, id);
        }
        return report;
    }

    public List<Map<String, Object>> runReport(String sql) {
        return executeValidatedQuery(sql, Collections.emptyMap());
    }

    public List<Map<String, Object>> runReport(String sql, Map<String, Object> parameters) {
        return executeValidatedQuery(sql, parameters);
    }

    public void createReport(Report report) {
        if (report.getName() == null || report.getName().isEmpty()) {
            throw new RuntimeException("名称不能为空");
        }
        if (report.getSql() == null || report.getSql().isEmpty()) {
            throw new RuntimeException("SQL不能为空");
        }
        validateSql(report.getSql());
        reportDao.save(report);
    }

    public Map<String, Object> generateReport(Long reportId, Map<String, Object> params) {
        Report report = getReportById(reportId);
        if (report == null) {
            throw new RuntimeException("报表不存在");
        }

        List<Map<String, Object>> data = runReport(report.getSql(), params);
        return Map.of(
                "reportName", report.getName(),
                "data", data,
                "count", data.size()
        );
    }

    private List<Map<String, Object>> executeValidatedQuery(String sql, Map<String, Object> parameters) {
        validateSql(sql);
        return reportDao.executeSql(sql, parameters == null ? Collections.emptyMap() : parameters);
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new RuntimeException("SQL不能为空");
        }
        String normalizedSql = sql.trim().toLowerCase();
        if (!(normalizedSql.startsWith("select") || normalizedSql.startsWith("with"))) {
            throw new RuntimeException("仅允许执行 SELECT 或 WITH 查询");
        }
        if (sql.contains(";")) {
            throw new RuntimeException("不允许执行多语句 SQL");
        }
        if (FORBIDDEN_SQL_PATTERN.matcher(sql).find()) {
            throw new RuntimeException("SQL 包含不允许的危险关键字");
        }
    }

    private User getCurrentUserSafely() {
        try {
            return currentUserService.getCurrentUserOrThrow();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}