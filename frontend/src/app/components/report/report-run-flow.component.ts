import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportAuditEvent, ReportService } from '../../services/report.service';

@Component({
  selector: 'app-report-run-flow',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flow-container" *ngIf="!loading && !error">
      <button class="back" (click)="goBack()">返回</button>
      <h1>报表审批流程（Run #{{ runId }}）</h1>

      <div *ngIf="events.length === 0">
        <p>暂无审计记录。</p>
      </div>

      <ul class="timeline" *ngIf="events.length > 0">
        <li *ngFor="let e of events">
          <div class="time">{{ e.eventTime | date:'yyyy-MM-dd HH:mm:ss' }}</div>
          <div class="content">
            <div class="type">{{ e.eventType }}</div>
            <div class="meta">
              <span *ngIf="e.actorUsername">用户：{{ e.actorUsername }}</span>
              <span *ngIf="e.actorRole">（角色：{{ e.actorRole }}）</span>
            </div>
            <div class="comment" *ngIf="e.comment">备注：{{ e.comment }}</div>
          </div>
        </li>
      </ul>
    </div>

    <div *ngIf="loading">加载审批流程中...</div>
    <div *ngIf="error" class="error">{{ error }}</div>
  `,
  styles: [`
    .flow-container {
      padding: 16px;
    }
    .back {
      margin-bottom: 12px;
    }
    .timeline {
      list-style: none;
      padding-left: 0;
      border-left: 2px solid #ddd;
      margin-left: 8px;
    }
    .timeline li {
      margin: 12px 0 12px 12px;
      position: relative;
    }
    .timeline li::before {
      content: '';
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: #4CAF50;
      position: absolute;
      left: -16px;
      top: 6px;
    }
    .time {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
    }
    .type {
      font-weight: bold;
    }
    .meta {
      font-size: 12px;
      color: #555;
    }
    .comment {
      margin-top: 4px;
      font-size: 13px;
    }
    .error {
      color: red;
      padding: 10px;
      background: #ffe6e6;
    }
  `]
})
export class ReportRunFlowComponent implements OnInit {
  runId!: number;
  events: ReportAuditEvent[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error = '缺少 runId 参数';
      return;
    }
    this.runId = +idParam;
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.error = null;
    this.reportService.getAuditTrail(this.runId).subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
      },
      error: (err) => {
        this.error = '加载审批流程失败: ' + (err.error?.message || err.message || '');
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/reports']);
  }
}
