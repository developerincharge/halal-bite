// src/app/layout/shell/shell.component.ts
import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-logo">
          <span class="logo-icon">🥙</span>
          <span class="logo-text">Halal Bite</span>
          <span class="logo-sub">Restaurant Portal</span>
        </div>

        <nav class="sidebar-nav">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">📊</span>
            <span>Dashboard</span>
          </a>
          <a routerLink="/orders" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">🧾</span>
            <span>Orders</span>
          </a>
          <a routerLink="/menu" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">🍽️</span>
            <span>Menu</span>
          </a>
        </nav>

        <div class="sidebar-footer">
          <div class="user-info">
            <div class="user-avatar">{{ userInitial }}</div>
            <div>
              <div class="user-email">{{ userEmail }}</div>
              <div class="user-role">Restaurant Owner</div>
            </div>
          </div>
          <button class="logout-btn" (click)="logout()">Sign Out</button>
        </div>
      </aside>

      <!-- Main content -->
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .shell {
      display: flex;
      height: 100vh;
      background: #f8f9fa;
    }

    .sidebar {
      width: 240px;
      background: #1a1a2e;
      color: white;
      display: flex;
      flex-direction: column;
      padding: 0;
      flex-shrink: 0;
    }

    .sidebar-logo {
      padding: 24px 20px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .logo-icon { font-size: 28px; }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #f4a261;
      letter-spacing: 0.5px;
    }

    .logo-sub {
      font-size: 11px;
      color: rgba(255,255,255,0.4);
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .sidebar-nav {
      flex: 1;
      padding: 16px 12px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      border-radius: 8px;
      color: rgba(255,255,255,0.6);
      text-decoration: none;
      font-size: 14px;
      transition: all 0.15s;
    }

    .nav-item:hover {
      background: rgba(255,255,255,0.08);
      color: white;
    }

    .nav-item.active {
      background: #f4a261;
      color: white;
      font-weight: 600;
    }

    .nav-icon { font-size: 18px; }

    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid rgba(255,255,255,0.1);
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .user-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: #f4a261;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 14px;
      flex-shrink: 0;
    }

    .user-email {
      font-size: 12px;
      color: rgba(255,255,255,0.8);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 140px;
    }

    .user-role {
      font-size: 10px;
      color: rgba(255,255,255,0.4);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .logout-btn {
      width: 100%;
      padding: 8px;
      background: rgba(255,255,255,0.08);
      color: rgba(255,255,255,0.6);
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      transition: all 0.15s;
    }

    .logout-btn:hover {
      background: rgba(220,53,69,0.3);
      color: #ff6b6b;
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 32px;
    }
  `]
})
export class ShellComponent {
  private authService = inject(AuthService);

  get userEmail() { return this.authService.currentUser()?.email ?? ''; }
  get userInitial() { return this.userEmail.charAt(0).toUpperCase(); }

  logout() { this.authService.logout(); }
}
