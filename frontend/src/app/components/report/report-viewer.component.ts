import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NotificationSummary, ReportRun, ReportAuditEvent, ReportService } from '../../services/report.service';

import { AuthService } from '../../services/auth.service';

interface Report {
  id: number;
  name: string;
  sql: string;
  description: string;
}

interface ReportData {
  data: any[];
  count?: number;
  custom?: boolean;
}

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-viewer.component.html',
  styleUrls: ['./report-viewer.component.css']
})
export class ReportViewerComponent implements OnInit {
  reports: Report[] = [];
  selectedReport: Report | null = null;
  reportData: ReportData | null = null;
  loading = false;
  error: string | null = null;
  exportError: string | null = null;
  parameterError: string | null = null;
  reportParametersText = '{}';

  // 登录相关状态
  username = 'admin';
  password = '123456';
  loggingIn = false;
  loginError: string | null = null;

  // 当前报表运行实例（Maker 视角）
  currentRun: ReportRun | null = null;
  submitMessage: string | null = null;
  submitError: string | null = null;

  currentRunAudit: ReportAuditEvent[] = [];
  currentRunAuditError: string | null = null;

  // Maker 历史运行列表
  makerRuns: ReportRun[] = [];
  makerRunsError: string | null = null;
  makerRunStatusFilter = '';
  makerRunReportNameFilter = '';
  makerRunsPage = 0;
  makerRunsSize = 10;
  makerRunsTotalPages = 0;
  makerRunsTotalElements = 0;

  // Checker 待办列表与审批状态
  checkerRuns: ReportRun[] = [];
  selectedCheckerRun: ReportRun | null = null;
  checkerRunReportNameFilter = '';
  checkerRunMakerFilter = '';
  checkerRunsPage = 0;
  checkerRunsSize = 10;
  checkerRunsTotalPages = 0;

  checkerDecision: 'APPROVED' | 'REJECTED' = 'APPROVED';

  checkerComment = '';
  checkerMessage: string | null = null;
  checkerError: string | null = null;

  checkerAudit: ReportAuditEvent[] = [];
  checkerAuditError: string | null = null;

  // Checker 历史审批记录
  checkerHistoryRuns: ReportRun[] = [];
  checkerHistoryError: string | null = null;
  checkerHistoryStatusFilter = '';
  checkerHistoryReportNameFilter = '';
  checkerHistoryPage = 0;
  checkerHistorySize = 10;
  checkerHistoryTotalPages = 0;

  notificationSummary: NotificationSummary | null = null;
  notificationError: string | null = null;

  // 中文报表名称映射
  private reportNameMap: { [key: string]: string } = {
    'Customer Transaction Analysis': '客户交易分析',
    'VIP Customer Revenue Report': 'VIP客户收入报告',
    'Merchant Performance Analysis': '商家绩效分析',
    'Department Budget Analysis': '部门预算分析',
    'Product Profitability Report': '产品盈利能力报告',
    'Customer Segmentation Analysis': '客户细分分析',
    'Monthly Revenue Trend Analysis': '月度收入趋势分析',
    'Order Fulfillment Analysis': '订单履行分析',
    'Employee Performance Metrics': '员工绩效指标',
    'Customer-Merchant Revenue Matrix': '客户商家收入矩阵',
    'Inventory Velocity Analysis': '库存周转分析',
    'Financial Health Scorecard': '财务健康仪表板'
  };

  // 中文报表描述映射
  private reportDescriptionMap: { [key: string]: string } = {
    'Customer Transaction Analysis': '综合客户交易分析，包含信用评分关联和平均交易计算',
    'VIP Customer Revenue Report': '详细VIP客户收入分析，包含账户余额和利润计算',
    'Merchant Performance Analysis': '分析商家绩效指标，包含交易量、计数和佣金估算',
    'Department Budget Analysis': '综合部门预算分析，比较分配预算与实际薪资成本及差异计算',
    'Product Profitability Report': '详细产品盈利能力分析，包含销售量、收入、成本和利润率',
    'Customer Segmentation Analysis': '基于交易行为、收入水平和价值分类的高级客户细分',
    'Monthly Revenue Trend Analysis': '显示收入、支出和交易计数的月度收入趋势分析',
    'Order Fulfillment Analysis': '跟踪订单量、价值和状态分布的订单履行分析',
    'Employee Performance Metrics': '包含薪资分布和部门预算影响的员工绩效分析',
    'Customer-Merchant Revenue Matrix': '显示客户和商家之间收入关系的交叉表分析及排名指标',
    'Inventory Velocity Analysis': '显示销售量和盈利能力指标的库存管理分析',
    'Financial Health Scorecard': '显示收入、支出、利润和客户指标等关键绩效指标的高管财务健康仪表板'
  };

  constructor(
    private reportService: ReportService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.loadReports();
      this.loadMakerRunsIfNeeded();
      this.loadCheckerDataIfNeeded();
      this.loadNotificationSummary();
    }
  }

  login() {
    this.loggingIn = true;
    this.loginError = null;
    this.error = null;
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loggingIn = false;
        this.loadReports();
        this.loadMakerRunsIfNeeded();
        this.loadCheckerDataIfNeeded();
        this.loadNotificationSummary();
      },
      error: (err: any) => {
        this.loggingIn = false;
        this.loginError = '登录失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  logout() {
    this.authService.logout();
    this.reports = [];
    this.selectedReport = null;
    this.reportData = null;
    this.error = null;
    this.exportError = null;
    this.parameterError = null;
    this.currentRun = null;
    this.submitMessage = null;
    this.submitError = null;
    this.currentRunAudit = [];
    this.currentRunAuditError = null;
    this.makerRuns = [];
    this.checkerRuns = [];
    this.selectedCheckerRun = null;
    this.checkerMessage = null;
    this.checkerError = null;
    this.checkerAudit = [];
    this.checkerAuditError = null;
    this.checkerHistoryRuns = [];
    this.checkerHistoryError = null;
    this.notificationSummary = null;
    this.notificationError = null;
    this.router.navigate(['/login']);
  }

  loadReports() {
    this.reportService.getReports().subscribe({
      next: (data: Report[]) => {
        this.reports = data;
      },
      error: (err: any) => {
        this.error = 'Failed to load reports: ' + (err.error?.message || err.message || '');
      }
    });
  }

  selectReport(reportId: string) {
    const report = this.reports.find((r) => r.id === +reportId) || null;
    this.selectedReport = report;
    this.reportData = null;
    this.error = null;
    this.submitMessage = null;
    this.submitError = null;
    this.currentRun = null;
    this.parameterError = null;
    this.currentRunAudit = [];
    if (report) {
      this.loadCurrentRunForSelectedReport();
    }
  }

  runReport() {
    if (!this.selectedReport) {
      return;
    }

    const parsedParameters = this.parseParameters();
    if (parsedParameters === null) {
      return;
    }

    this.loading = true;
    this.error = null;
    this.parameterError = null;
    this.reportService.executeReport(this.selectedReport.id, parsedParameters).subscribe({
      next: (data: Array<Record<string, unknown>>) => {
        this.reportData = { data, count: data.length };
        this.loading = false;
        this.loadCurrentRunForSelectedReport();
        this.loadMakerRuns();
        this.loadNotificationSummary();
      },
      error: (err: any) => {
        this.error = 'Failed to execute report: ' + (err.error?.message || err.message || '');
        this.loading = false;
      }
    });
  }

  exportReport() {
    if (!this.selectedReport) {
      return;
    }
    this.exportError = null;
    this.reportService.downloadReport(this.selectedReport.id).subscribe({
      next: (blob: Blob) => {
        const baseName = this.getChineseReportName(this.selectedReport?.name || '') || 'report-' + this.selectedReport?.id;
        this.triggerDownload(blob, baseName + '.xlsx');
      },
      error: (err: any) => {
        this.exportError = '导出失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  exportCurrentRun() {
    if (!this.currentRun) {
      return;
    }
    this.exportError = null;
    this.reportService.downloadRun(this.currentRun.id).subscribe({
      next: (blob: Blob) => {
        const baseName = this.currentRun?.reportName || 'report-run-' + this.currentRun?.id;
        this.triggerDownload(blob, baseName + '.xlsx');
      },
      error: (err: any) => {
        this.exportError = '导出失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  exportRun(run: ReportRun) {
    this.exportError = null;
    this.reportService.downloadRun(run.id).subscribe({
      next: (blob: Blob) => {
        const baseName = run.reportName || 'report-run-' + run.id;
        this.triggerDownload(blob, baseName + '.xlsx');
      },
      error: (err: any) => {
        this.exportError = '导出失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  submitCurrentRun() {
    if (!this.currentRun || (this.currentRun.status !== 'Generated' && this.currentRun.status !== 'Rejected')) {
      return;
    }
    this.submitMessage = null;
    this.submitError = null;
    this.reportService.submitRun(this.currentRun.id).subscribe({
      next: () => {
        this.submitMessage = this.currentRun?.status === 'Rejected' ? '已重新提交审批' : '已提交审批';
        this.loadCurrentRunForSelectedReport();
        this.loadMakerRuns();
        this.loadCheckerDataIfNeeded();
        this.loadNotificationSummary();
      },
      error: (err: any) => {
        this.submitError = '提交审批失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  loadMakerRuns() {
    this.makerRunsError = null;
    this.reportService.getMyRuns({
      status: this.makerRunStatusFilter || undefined,
      reportName: this.makerRunReportNameFilter || undefined,
      page: this.makerRunsPage,
      size: this.makerRunsSize
    }).subscribe({
      next: (response: any) => {
        this.makerRuns = response.content;
        this.makerRunsTotalPages = response.totalPages;
        this.makerRunsTotalElements = response.totalElements;
      },
      error: (err: any) => {
        this.makerRuns = [];
        this.makerRunsError = '加载我的提交失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  loadCheckerRuns() {
    this.checkerMessage = null;
    this.checkerError = null;
    this.reportService.getSubmittedRuns({
      reportName: this.checkerRunReportNameFilter || undefined,
      makerUsername: this.checkerRunMakerFilter || undefined,
      page: this.checkerRunsPage,
      size: this.checkerRunsSize
    }).subscribe({
      next: (response: any) => {
        this.checkerRuns = response.content;
        this.checkerRunsTotalPages = response.totalPages;
        if (!this.selectedCheckerRun && this.checkerRuns.length > 0) {
          this.selectedCheckerRun = this.checkerRuns[0];
          this.loadCheckerAudit();
        }
      },
      error: (err: any) => {
        this.checkerError = '加载待审批报表失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  loadCheckerHistory() {
    this.checkerHistoryError = null;
    this.reportService.getCheckerHistoryRuns({
      status: this.checkerHistoryStatusFilter || undefined,
      reportName: this.checkerHistoryReportNameFilter || undefined,
      page: this.checkerHistoryPage,
      size: this.checkerHistorySize
    }).subscribe({
      next: (response: any) => {
        this.checkerHistoryRuns = response.content;
        this.checkerHistoryTotalPages = response.totalPages;
      },
      error: (err: any) => {
        this.checkerHistoryRuns = [];
        this.checkerHistoryError = '加载历史审批记录失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  selectCheckerRun(runId: string) {
    this.selectedCheckerRun = this.checkerRuns.find((r) => r.id === +runId) || null;
    this.checkerComment = '';
    this.checkerMessage = null;
    this.checkerError = null;
    this.loadCheckerAudit();
  }

  decideSelectedRun() {
    if (!this.selectedCheckerRun) {
      return;
    }
    this.checkerMessage = null;
    this.checkerError = null;
    this.reportService.decideRun(this.selectedCheckerRun.id, this.checkerDecision, this.checkerComment).subscribe({
      next: () => {
        this.checkerMessage = this.checkerDecision === 'APPROVED' ? '已批准' : '已拒绝';
        this.checkerComment = '';
        this.loadCheckerRuns();
        this.loadCheckerHistory();
        this.loadNotificationSummary();
        this.checkerAudit = [];
        this.checkerAuditError = null;
      },
      error: (err: any) => {
        this.checkerError = '审批操作失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  loadNotificationSummary() {
    this.notificationError = null;
    this.reportService.getNotificationSummary().subscribe({
      next: (summary: NotificationSummary) => {
        this.notificationSummary = summary;
      },
      error: (err: any) => {
        this.notificationSummary = null;
        this.notificationError = '加载通知摘要失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  getChineseReportName(englishName: string): string {
    return this.reportNameMap[englishName] || englishName;
  }

  getChineseReportDescription(englishName: string): string {
    return this.reportDescriptionMap[englishName] || englishName;
  }

  formatParameters(parametersJson?: string): string {
    if (!parametersJson) {
      return '无';
    }
    try {
      return JSON.stringify(JSON.parse(parametersJson), null, 2);
    } catch {
      return parametersJson;
    }
  }

  previousMakerRunsPage() {
    if (this.makerRunsPage > 0) {
      this.makerRunsPage -= 1;
      this.loadMakerRuns();
    }
  }

  nextMakerRunsPage() {
    if (this.makerRunsPage + 1 < this.makerRunsTotalPages) {
      this.makerRunsPage += 1;
      this.loadMakerRuns();
    }
  }

  previousCheckerRunsPage() {
    if (this.checkerRunsPage > 0) {
      this.checkerRunsPage -= 1;
      this.loadCheckerRuns();
    }
  }

  nextCheckerRunsPage() {
    if (this.checkerRunsPage + 1 < this.checkerRunsTotalPages) {
      this.checkerRunsPage += 1;
      this.loadCheckerRuns();
    }
  }

  previousCheckerHistoryPage() {
    if (this.checkerHistoryPage > 0) {
      this.checkerHistoryPage -= 1;
      this.loadCheckerHistory();
    }
  }

  nextCheckerHistoryPage() {
    if (this.checkerHistoryPage + 1 < this.checkerHistoryTotalPages) {
      this.checkerHistoryPage += 1;
      this.loadCheckerHistory();
    }
  }

  getKeys(data: Array<Record<string, unknown>>): string[] {
    if (!data || data.length === 0) {
      return [];
    }
    return Object.keys(data[0]);
  }

  viewRunFlow(runId: number) {
    this.router.navigate(['/runs', runId, 'flow']);
  }

  private loadMakerRunsIfNeeded() {
    const user = this.authService.getCurrentUser();
    if (user?.role?.includes('MAKER')) {
      this.loadMakerRuns();
    }
  }

  private loadCheckerDataIfNeeded() {
    const user = this.authService.getCurrentUser();
    if (user?.role?.includes('CHECKER')) {
      this.loadCheckerRuns();
      this.loadCheckerHistory();
    }
  }

  private loadCurrentRunForSelectedReport() {
    if (!this.selectedReport) {
      return;
    }
    this.reportService.getMyLatestRun(this.selectedReport.id).subscribe({
      next: (run: ReportRun) => {
        this.currentRun = run;
        this.loadCurrentRunAudit();
      },
      error: (err: any) => {
        this.currentRun = null;
        this.currentRunAudit = [];
        this.currentRunAuditError = null;
      }
    });
  }

  private loadCurrentRunAudit() {
    if (!this.currentRun) {
      this.currentRunAudit = [];
      this.currentRunAuditError = null;
      return;
    }
    this.currentRunAuditError = null;
    this.reportService.getAuditTrail(this.currentRun.id).subscribe({
      next: (events: ReportAuditEvent[]) => {
        this.currentRunAudit = events;
      },
      error: (err: any) => {
        this.currentRunAudit = [];
        this.currentRunAuditError = '加载审计轨迹失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  private loadCheckerAudit() {
    if (!this.selectedCheckerRun) {
      this.checkerAudit = [];
      this.checkerAuditError = null;
      return;
    }
    this.checkerAuditError = null;
    this.reportService.getAuditTrail(this.selectedCheckerRun.id).subscribe({
      next: (events: ReportAuditEvent[]) => {
        this.checkerAudit = events;
      },
      error: (err: any) => {
        this.checkerAudit = [];
        this.checkerAuditError = '加载审计轨迹失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  private parseParameters(): Record<string, unknown> | null {
    const trimmed = this.reportParametersText.trim();
    if (!trimmed) {
      return {};
    }
    try {
      const parsed = JSON.parse(trimmed) as unknown;
      if (parsed === null || Array.isArray(parsed) || typeof parsed !== 'object') {
        this.parameterError = '报表参数必须是 JSON 对象，例如 {"status":"SUCCESS"}';
        return null;
      }
      return parsed as Record<string, unknown>;
    } catch {
      this.parameterError = '报表参数不是合法 JSON，请输入对象格式。';
      return null;
    }
  }

  private triggerDownload(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}