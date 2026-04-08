package com.legacy.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.model.Report;
import com.legacy.report.model.ReportAuditEvent;
import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import com.legacy.report.repository.ReportAuditEventRepository;
import com.legacy.report.repository.ReportRunRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportRunService {

    private static final Logger logger = LoggerFactory.getLogger(ReportRunService.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRunRepository reportRunRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportAuditEventRepository reportAuditEventRepository;

    private Counter generatedCounter;
    private Counter submittedCounter;
    private Counter approvedCounter;
    private Counter rejectedCounter;
    private Timer approvalDurationTimer;

    @Autowired(required = false)
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            this.generatedCounter = Counter.builder("report_run_generated_total")
                    .description("Number of report runs generated")
                    .register(meterRegistry);
            this.submittedCounter = Counter.builder("report_run_submitted_total")
                    .description("Number of report runs submitted for approval")
                    .register(meterRegistry);
            this.approvedCounter = Counter.builder("report_run_approved_total")
                    .description("Number of report runs approved")
                    .register(meterRegistry);
            this.rejectedCounter = Counter.builder("report_run_rejected_total")
                    .description("Number of report runs rejected")
                    .register(meterRegistry);
            this.approvalDurationTimer = Timer.builder("report_run_approval_duration_seconds")
                    .description("Time between report generation and final decision in seconds")
                    .register(meterRegistry);
        }
    }

    @Transactional
    public List<Map<String, Object>> executeReportWithRun(Long reportId, Map<String, Object> parameters) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        Report report = reportService.getReportById(reportId);
        if (report == null) {
            throw new RuntimeException("报表不存在");
        }

        logger.info("event=report_run_execute_start reportId={} maker={} hasParameters={}",
                reportId, currentUser.getUsername(), parameters != null && !parameters.isEmpty());

        List<Map<String, Object>> data = reportService.runReport(report.getSql(), parameters);

        ReportRun run = new ReportRun();
        run.setReportId(report.getId());
        run.setReportName(report.getName());
        run.setStatus("Generated");
        run.setMakerUsername(currentUser.getUsername());
        run.setGeneratedAt(LocalDateTime.now());
        run.setParametersJson(writeJsonSafely(parameters));
        run.setResultSnapshot(writeJsonSafely(data));

        ReportRun saved = reportRunRepository.save(run);

        auditService.recordEvent(
                saved.getId(),
                report.getId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                "Generated",
                parameters == null || parameters.isEmpty() ? null : "执行参数已保存"
        );

        if (generatedCounter != null) {
            generatedCounter.increment();
        }

        logger.info("event=report_run_execute_success runId={} reportId={} maker={}",
                saved.getId(), saved.getReportId(), currentUser.getUsername());

        return data;
    }

    @Transactional
    public ReportRun submitRun(Long runId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

        if (!("Generated".equals(run.getStatus()) || "Rejected".equals(run.getStatus()))) {
            throw new RuntimeException("只能提交 Generated 或 Rejected 状态的报表运行实例");
        }

        if (!currentUser.getUsername().equals(run.getMakerUsername())) {
            throw new RuntimeException("只能提交由当前 Maker 自己生成的报表运行实例");
        }

        boolean resubmission = "Rejected".equals(run.getStatus());
        run.setStatus("Submitted");
        run.setSubmittedAt(LocalDateTime.now());
        run.setCheckerUsername(null);
        run.setDecidedAt(null);

        ReportRun saved = reportRunRepository.save(run);

        auditService.recordEvent(
                saved.getId(),
                saved.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                resubmission ? "Resubmitted" : "Submitted",
                null
        );

        if (submittedCounter != null) {
            submittedCounter.increment();
        }

        logger.info("event=report_run_submit_success runId={} reportId={} maker={} resubmission={}",
                saved.getId(), saved.getReportId(), currentUser.getUsername(), resubmission);

        return saved;
    }

    @Transactional
    public ReportRun decideRun(Long runId, boolean approve, String comment) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "CHECKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

        if (!"Submitted".equals(run.getStatus())) {
            throw new RuntimeException("只能对 Submitted 状态的报表运行实例进行审批");
        }

        if (!approve && (comment == null || comment.trim().isEmpty())) {
            throw new RuntimeException("拒绝审批时必须填写 comment");
        }

        currentUserService.requireReportAccess(currentUser, run.getReportId());

        run.setCheckerUsername(currentUser.getUsername());
        run.setDecidedAt(LocalDateTime.now());
        run.setStatus(approve ? "Approved" : "Rejected");

        ReportRun saved = reportRunRepository.save(run);

        auditService.recordEvent(
                saved.getId(),
                saved.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                approve ? "Approved" : "Rejected",
                comment
        );

        if (approve) {
            if (approvedCounter != null) {
                approvedCounter.increment();
            }
        } else if (rejectedCounter != null) {
            rejectedCounter.increment();
        }

        if (approvalDurationTimer != null && run.getGeneratedAt() != null && run.getDecidedAt() != null) {
            approvalDurationTimer.record(Duration.between(run.getGeneratedAt(), run.getDecidedAt()));
        }

        logger.info("event=report_run_decision_success runId={} reportId={} checker={} decision={} commentPresent={}",
                saved.getId(), saved.getReportId(), currentUser.getUsername(),
                approve ? "Approved" : "Rejected",
                comment != null && !comment.trim().isEmpty());

        return saved;
    }

    public ReportRun getLatestRunForCurrentMaker(Long reportId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");
        currentUserService.requireReportAccess(currentUser, reportId);

        List<ReportRun> runs = reportRunRepository
                .findByMakerUsernameAndReportIdOrderByGeneratedAtDesc(currentUser.getUsername(), reportId);

        if (runs.isEmpty()) {
            throw new RuntimeException("当前用户在该报表下没有执行记录");
        }

        return runs.get(0);
    }

    public Map<String, Object> getRunsForCurrentMaker(String status, String reportName, int page, int size) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        List<ReportRun> runs = reportRunRepository.findByMakerUsernameOrderByGeneratedAtDesc(currentUser.getUsername()).stream()
                .filter(run -> currentUserService.hasReportAccess(currentUser, run.getReportId()))
                .filter(run -> matchesStatus(run, status))
                .filter(run -> matchesReportName(run, reportName))
                .collect(Collectors.toList());

        return toPagedResponse(runs, page, size);
    }

    public Map<String, Object> getSubmittedRunsForChecker(String reportName, String makerUsername, int page, int size) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "CHECKER");

        List<ReportRun> runs = reportRunRepository.findAll().stream()
                .filter(run -> "Submitted".equals(run.getStatus()))
                .filter(run -> currentUserService.hasReportAccess(currentUser, run.getReportId()))
                .filter(run -> matchesReportName(run, reportName))
                .filter(run -> makerUsername == null || makerUsername.isBlank() || run.getMakerUsername().equalsIgnoreCase(makerUsername.trim()))
                .sorted(Comparator.comparing(ReportRun::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        return toPagedResponse(runs, page, size);
    }

    public Map<String, Object> getHistoryRunsForCurrentChecker(String status, String reportName, int page, int size) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "CHECKER");

        List<ReportRun> runs = reportRunRepository.findByCheckerUsernameOrderByDecidedAtDesc(currentUser.getUsername()).stream()
                .filter(run -> matchesStatus(run, status))
                .filter(run -> matchesReportName(run, reportName))
                .collect(Collectors.toList());

        return toPagedResponse(runs, page, size);
    }

    public Map<String, Object> getNotificationSummaryForCurrentUser() {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        List<ReportRun> visibleRuns = reportRunRepository.findAll().stream()
                .filter(run -> currentUserService.hasReportAccess(currentUser, run.getReportId()))
                .collect(Collectors.toList());

        long pendingApprovals = visibleRuns.stream()
                .filter(run -> "Submitted".equals(run.getStatus()))
                .count();
        long myRejectedRuns = visibleRuns.stream()
                .filter(run -> Objects.equals(run.getMakerUsername(), currentUser.getUsername()))
                .filter(run -> "Rejected".equals(run.getStatus()))
                .count();
        long myApprovedRuns = visibleRuns.stream()
                .filter(run -> Objects.equals(run.getMakerUsername(), currentUser.getUsername()))
                .filter(run -> "Approved".equals(run.getStatus()))
                .count();

        List<String> notifications = visibleRuns.stream()
                .filter(run -> Objects.equals(run.getMakerUsername(), currentUser.getUsername()) || Objects.equals(run.getCheckerUsername(), currentUser.getUsername()))
                .sorted(Comparator.comparing(ReportRun::getGeneratedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(run -> "#" + run.getId() + " " + run.getReportName() + " 当前状态为 " + run.getStatus())
                .collect(Collectors.toList());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pendingApprovals", pendingApprovals);
        summary.put("myRejectedRuns", myRejectedRuns);
        summary.put("myApprovedRuns", myApprovedRuns);
        summary.put("notifications", notifications);
        return summary;
    }

    public List<ReportAuditEvent> getAuditEventsForRun(Long reportRunId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        ReportRun run = reportRunRepository.findById(reportRunId)
                .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));
        currentUserService.requireReportAccess(currentUser, run.getReportId());
        return reportAuditEventRepository.findByReportRunIdOrderByEventTimeAsc(reportRunId);
    }

    private String writeJsonSafely(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private boolean matchesStatus(ReportRun run, String status) {
        return status == null || status.isBlank() || run.getStatus().equalsIgnoreCase(status.trim());
    }

    private boolean matchesReportName(ReportRun run, String reportName) {
        return reportName == null || reportName.isBlank()
                || (run.getReportName() != null && run.getReportName().toLowerCase().contains(reportName.trim().toLowerCase()));
    }

    private Map<String, Object> toPagedResponse(List<ReportRun> runs, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int totalElements = runs.size();
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", runs.subList(fromIndex, toIndex));
        response.put("page", safePage);
        response.put("size", safeSize);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        return response;
    }
}
