import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { HttpParams } from '@angular/common/http';

interface Report {
  id: number;
  name: string;
  sql: string;
  description: string;
}

interface CreateReportRequest {
  name: string;
  sql: string;
  description?: string;
}

export interface ReportRun {
  id: number;
  reportId: number;
  reportName: string;
  status: string;
  makerUsername: string;
  checkerUsername?: string;
  generatedAt: string;
  submittedAt?: string;
  decidedAt?: string;
  parametersJson?: string;
}

export interface ReportAuditEvent {
  id: number;
  reportRunId: number;
  reportId: number;
  actorUsername: string;
  actorRole?: string;
  eventType: string;
  eventTime: string;
  comment?: string;
}

export interface RunListFilter {
  status?: string;
  reportName?: string;
  makerUsername?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface NotificationSummary {
  pendingApprovals: number;
  myRejectedRuns: number;
  myApprovedRuns: number;
  notifications: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api';

  private reportsSubject = new BehaviorSubject<Report[]>([]);
  reports$ = this.reportsSubject.asObservable();

  constructor(private http: HttpClient) { }

  getReports(): Observable<Report[]> {
    return this.http.get<Report[]>(this.apiUrl + '/reports');
  }

  getReport(id: number): Observable<Report> {
    return this.http.get<Report>(this.apiUrl + '/reports/' + id);
  }

  executeReport(id: number, params: Record<string, unknown>): Observable<any[]> {
    return this.http.post<any[]>(this.apiUrl + '/reports/' + id + '/execute', {
      params
    });
  }

  generateReport(reportId: number, params: Record<string, unknown>): Observable<any> {
    return this.http.post(this.apiUrl + '/reports/generate', {
      reportId: reportId,
      params: params
    });
  }

  createReport(report: CreateReportRequest): Observable<void> {
    return this.http.post<void>(this.apiUrl + '/reports', report);
  }

  // 报表运行相关接口

  getMyLatestRun(reportId: number): Observable<ReportRun> {
    return this.http.get<ReportRun>(`${this.apiUrl}/report-runs/my-latest`, {
      params: { reportId: reportId.toString() }
    });
  }

  getSubmittedRuns(filter: RunListFilter): Observable<PagedResponse<ReportRun>> {
    return this.http.get<PagedResponse<ReportRun>>(`${this.apiUrl}/report-runs/submitted`, {
      params: this.buildRunListParams(filter)
    });
  }

  getMyRuns(filter: RunListFilter): Observable<PagedResponse<ReportRun>> {
    return this.http.get<PagedResponse<ReportRun>>(`${this.apiUrl}/report-runs/my-runs`, {
      params: this.buildRunListParams(filter)
    });
  }

  getCheckerHistoryRuns(filter: RunListFilter): Observable<PagedResponse<ReportRun>> {
    return this.http.get<PagedResponse<ReportRun>>(`${this.apiUrl}/report-runs/checker/history`, {
      params: this.buildRunListParams(filter)
    });
  }

  getNotificationSummary(): Observable<NotificationSummary> {
    return this.http.get<NotificationSummary>(`${this.apiUrl}/report-runs/notifications/summary`);
  }

  submitRun(runId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/report-runs/${runId}/submit`, {});
  }

  decideRun(runId: number, decision: 'APPROVED' | 'REJECTED', comment: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/report-runs/${runId}/decision`, {
      decision,
      comment
    });
  }

  getAuditTrail(runId: number): Observable<ReportAuditEvent[]> {
    return this.http.get<ReportAuditEvent[]>(`${this.apiUrl}/report-runs/${runId}/audit`);
  }

  downloadReport(reportId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/reports/${reportId}/export`, {
      responseType: 'blob'
    });
  }

  downloadRun(runId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/report-runs/${runId}/export`, {
      responseType: 'blob'
    });
  }

  private buildRunListParams(filter: RunListFilter): HttpParams {
    let params = new HttpParams();
    if (filter.status) {
      params = params.set('status', filter.status);
    }
    if (filter.reportName) {
      params = params.set('reportName', filter.reportName);
    }
    if (filter.makerUsername) {
      params = params.set('makerUsername', filter.makerUsername);
    }
    params = params.set('page', (filter.page ?? 0).toString());
    params = params.set('size', (filter.size ?? 10).toString());
    return params;
  }
}