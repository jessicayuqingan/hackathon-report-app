# Agent Documentation

## Overview

This document provides information about the agents used in this report application project.

## Project Structure

This is a full-stack application consisting of:
- **Frontend**: Angular application (port 4200)
- **Backend**: Spring Boot application (port 8080)

## Getting Started

### Prerequisites

Minimum recommended environment (Windows 开发机)：

- Node.js 18+ 和 npm（用于前端构建与开发服务器）
- Java JDK 17 或更高版本
- Git（用于代码管理）
- 代理或网络环境允许访问 npm 与 Maven Central/Gradle 仓库

本项目已经内置 **Gradle Wrapper**（`backend/gradlew`、`backend/gradlew.bat`），
后端不需要全局安装 Gradle 或 Maven。

### First-time setup（第一次拉代码后）

在仓库根目录下：

1. 安装前端依赖：
   ```bash
   cd frontend
   npm install
   ```

2. （可选）验证前端单元测试：
   ```bash
   npm test
   ```

3. 返回根目录准备启动后端：
   ```bash
   cd ..
   ```

### Running the Application Locally

#### 1. 启动后端（Spring Boot, Gradle Wrapper）

在 `backend/` 目录：

```bash
cd backend
./gradlew bootRun        # Linux/macOS
:: 或
 .\gradlew bootRun       # Windows PowerShell/CMD
```

默认行为：

- 服务启动在 `http://localhost:8080`
- 内存 H2 数据库自动初始化，包括默认用户：
  - `admin / 123456`（拥有 MAKER,CHECKER 角色）
  - `maker1 / 123456`（MAKER）
  - `checker1 / 123456`（CHECKER）

#### 2. 启动前端（Angular）

在另一个终端窗口中，从仓库根目录：

```bash
cd frontend
npm start       # 等价于 ng serve --port 4200
```

默认行为：

- 前端开发服务器在 `http://localhost:4200`
- 所有以 `/api` 开头的请求通过浏览器直接调用 `http://localhost:8080/api/**`

#### 3. 访问应用

- 前端入口：`http://localhost:4200`
  - 首先打开登录页 `/login`
  - 登录后根据角色跳转：
    - Maker → `/maker`
    - Checker → `/checker`
  - 也可以直接访问通用报表页 `/reports`（需登录）

### Basic Testing Flow（手工联调流程）

1. **登录验证**
   - 使用 `maker1 / 123456` 登录，确认跳转到 Maker 视图
   - 使用 `checker1 / 123456` 登录，确认跳转到 Checker 视图

2. **Maker → Checker 审批流**
   - Maker：在 `/maker` 选择一个报表并执行，查看执行结果
   - Maker：在“当前报表运行状态”区块点击“提交审批”
   - Checker：在 `/checker` 刷新待审批列表，选择该运行，进行批准或拒绝，并可看到审计轨迹
   - 任一角色：在“查看完整审批流程”页面查看单次运行的完整审批时间线

3. **导出测试**
   - Maker：从报表结果或单次运行视图导出 Excel
   - Checker：对已批准的运行，从历史记录视图导出 Excel

如需进一步自动化测试：

- 后端：在 `backend/` 下运行 `./gradlew test` 或 `.\gradlew test`
- 前端：在 `frontend/` 下运行 `npm test`（Karma + Jasmine）

## Agent Configuration

[Add agent-specific configuration details here]

## API Endpoints

Backend (Spring Boot, port 8080):

- **Authentication**
  - `POST /api/auth/login` – Username/password login, returns a JWT (default user: `admin` / `123456`)
  - `GET /api/auth/profile` – Get current user profile using the JWT in `Authorization: Bearer <token>`
  - `POST /api/auth/logout` – Logical logout (frontend clears stored token)

- **Reports**
  - `GET /api/reports` – List all reports (requires valid JWT)
  - `GET /api/reports/{id}` – Get a single report definition (requires valid JWT)
  - `POST /api/reports/{id}/execute` – Execute a predefined report (requires valid JWT)
  - `POST /api/report-runs/{id}/submit` – Submit a generated report run for approval (Maker only)
  - `POST /api/report-runs/{id}/decision` – Approve or reject a submitted report run (Checker only)
  - `GET /api/report-runs/{id}/audit` – Get the audit/approval flow timeline for a report run

Frontend (Angular, port 4200):

- Provides a dedicated login page and a Maker/Checker report viewer UI:
  - Makers can execute predefined reports, view their latest runs, submit runs for approval, and download generated files.
  - Checkers can list submitted runs, inspect their audit/approval timeline, make approval decisions, and download runs when allowed.
  - The frontend no longer exposes any custom/ad-hoc SQL query UI.

## Development Notes

[Add development-specific notes and guidelines]
