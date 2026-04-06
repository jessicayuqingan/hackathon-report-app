package com.legacy.report.service;

import com.legacy.report.model.ReportAuditEvent;
import com.legacy.report.repository.ReportAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private ReportAuditEventRepository reportAuditEventRepository;

    public void recordEvent(Long reportRunId,
                            Long reportId,
                            String actorUsername,
                            String actorRole,
                            String eventType,
                            String comment) {
        ReportAuditEvent event = new ReportAuditEvent();
        event.setReportRunId(reportRunId);
        event.setReportId(reportId);
        event.setActorUsername(actorUsername);
        event.setActorRole(actorRole);
        event.setEventType(eventType);
        event.setEventTime(LocalDateTime.now());
        event.setComment(comment);
        reportAuditEventRepository.save(event);

        logger.info("event=report_audit_recorded runId={} reportId={} actor={} role={} type={} commentPresent={}",
                reportRunId,
                reportId,
                actorUsername,
                actorRole,
                eventType,
                comment != null && !comment.trim().isEmpty());
    }
}
