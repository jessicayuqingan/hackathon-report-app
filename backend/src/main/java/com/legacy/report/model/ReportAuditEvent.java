package com.legacy.report.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_audit_event")
public class ReportAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的报表运行实例 ID
    @Column(name = "report_run_id", nullable = false)
    private Long reportRunId;

    // 关联的报表模板 ID（冗余一份方便查询）
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    // 触发事件的用户名
    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    // 触发事件时的角色信息（例如 MAKER、CHECKER 或组合）
    @Column(name = "actor_role")
    private String actorRole;

    // 事件类型：Generated / Submitted / Approved / Rejected
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    // 审批意见等附加信息
    @Lob
    @Column(name = "comment")
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportRunId() {
        return reportRunId;
    }

    public void setReportRunId(Long reportRunId) {
        this.reportRunId = reportRunId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
