// src/app/features/auth/register/register.component.ts
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-logo">🥙</div>
        <h1 class="auth-title">Join Halal Bite</h1>
        <p class="auth-subtitle">Create your Restaurant Owner account</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="auth-form">
          <div class="field">
            <label>Email</label>
            <input type="email" formControlName="email" placeholder="owner@restaurant.com"
              [class.error]="form.get('email')?.invalid && form.get('email')?.touched" />
          </div>

          <div class="field">
            <label>Password <span class="hint">(min 8 characters)</span></label>
            <input type="password" formControlName="password" placeholder="••••••••"
              [class.error]="form.get('password')?.invalid && form.get('password')?.touched" />
          </div>

          @if (error()) {
            <div class="alert-error">{{ error() }}</div>
          }

          <button type="submit" class="btn-primary" [disabled]="loading()">
            {{ loading() ? 'Creating account...' : 'Create Restaurant Account' }}
          </button>
        </form>

        <p class="auth-footer">
          Already have an account?
          <a routerLink="/login">Sign in</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
    .auth-card {
      background: white;
      border-radius: 16px;
      padding: 40px;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      text-align: center;
    }
    .auth-logo { font-size: 48px; margin-bottom: 8px; }
    .auth-title { font-size: 24px; font-weight: 700; color: #1a1a2e; margin: 0 0 4px; }
    .auth-subtitle { color: #666; margin: 0 0 32px; font-size: 14px; }
    .auth-form { text-align: left; }
    .field { margin-bottom: 20px; }
    .field label { display: block; font-size: 13px; font-weight: 600; color: #333; margin-bottom: 6px; }
    .hint { font-weight: 400; color: #999; }
    .field input {
      width: 100%; padding: 12px 14px;
      border: 1.5px solid #e0e0e0; border-radius: 8px;
      font-size: 14px; box-sizing: border-box;
      transition: border-color 0.15s; outline: none;
    }
    .field input:focus { border-color: #f4a261; }
    .field input.error { border-color: #dc3545; }
    .alert-error {
      background: #fff5f5; color: #dc3545;
      padding: 10px 14px; border-radius: 8px;
      font-size: 13px; margin-bottom: 16px;
      border: 1px solid #fecaca;
    }
    .btn-primary {
      width: 100%; padding: 13px;
      background: #f4a261; color: white;
      border: none; border-radius: 8px;
      font-size: 15px; font-weight: 600;
      cursor: pointer; transition: background 0.15s;
    }
    .btn-primary:hover:not(:disabled) { background: #e76f51; }
    .btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }
    .auth-footer { margin-top: 20px; font-size: 13px; color: #666; }
    .auth-footer a { color: #f4a261; text-decoration: none; font-weight: 600; }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.error.set('');

    this.authService.register({
      ...this.form.value as any,
      role: 'RESTAURANT_OWNER'
    }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Registration failed. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
