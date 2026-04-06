package com.legacy.report.repository;

import com.legacy.report.model.ReportAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportAuditEventRepository extends JpaRepository<ReportAuditEvent, Long> {

    List<ReportAuditEvent> findByReportRunIdOrderByEventTimeAsc(Long reportRunId);
}
