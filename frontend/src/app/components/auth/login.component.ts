import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h1>报表管理系统登录</h1>

      <div *ngIf="authService.isLoggedIn(); else loginForm">
        <p>当前已登录用户：{{ authService.getCurrentUser()?.username }}</p>
        <button (click)="goAfterLogin()">进入系统</button>
      </div>

      <ng-template #loginForm>
        <form (ngSubmit)="onSubmit()" class="login-form">
          <label>
            用户名：
            <input [(ngModel)]="username" name="username" required />
          </label>

          <label>
            密码：
            <input type="password" [(ngModel)]="password" name="password" required />
          </label>

          <button type="submit" [disabled]="loggingIn">登录</button>

          <div *ngIf="loginError" class="error">{{ loginError }}</div>
        </form>
      </ng-template>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 400px;
      margin: 80px auto;
      padding: 24px;
      border: 1px solid #ddd;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    .login-form label {
      display: block;
      margin: 12px 0;
    }
    .login-form input {
      width: 100%;
      padding: 8px;
      box-sizing: border-box;
    }
    button {
      padding: 8px 16px;
      background: #4CAF50;
      color: white;
      border: none;
      cursor: pointer;
      margin-top: 8px;
    }
    button[disabled] {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .error {
      color: red;
      margin-top: 8px;
    }
  `]
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  loggingIn = false;
  loginError: string | null = null;
  private redirectUrl: string | null = null;

  constructor(
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.redirectUrl = this.route.snapshot.queryParamMap.get('redirect');
  }

  onSubmit(): void {
    if (!this.username || !this.password) {
      return;
    }
    this.loggingIn = true;
    this.loginError = null;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loggingIn = false;
        this.goAfterLogin();
      },
      error: (err) => {
        this.loggingIn = false;
        this.loginError = '登录失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  goAfterLogin(): void {
    const user = this.authService.getCurrentUser();
    let defaultTarget = '/reports';
    const role = user?.role || '';
    if (role.includes('CHECKER')) {
      defaultTarget = '/checker';
    } else if (role.includes('MAKER')) {
      defaultTarget = '/maker';
    }
    const target = this.redirectUrl || defaultTarget;
    this.router.navigateByUrl(target);
  }
}
