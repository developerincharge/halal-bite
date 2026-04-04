// src/app/core/auth/auth.service.ts
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../../shared/models';

/**
 * AuthService — manages authentication state for the entire app
 *
 * Uses Angular signals for reactive state management.
 * The JWT token is stored in localStorage so it persists
 * across browser refreshes.
 *
 * Key methods:
 *   login()    → POST /auth/login, store token
 *   logout()   → clear token, redirect to login
 *   isLoggedIn → computed signal, true if valid token exists
 *   currentUser → the decoded token payload
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'halalbite_token';
  private readonly USER_KEY  = 'halalbite_user';

  // Angular signals — reactive state
  private _currentUser = signal<AuthResponse | null>(this.loadUser());
  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoggedIn = computed(() => !!this._currentUser());
  readonly isOwner = computed(() =>
    this._currentUser()?.role === 'RESTAURANT_OWNER'
  );

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap(response => this.storeSession(response)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/register`, request)
      .pipe(tap(response => this.storeSession(response)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
    this._currentUser.set(response);
  }

  private loadUser(): AuthResponse | null {
    const stored = localStorage.getItem(this.USER_KEY);
    return stored ? JSON.parse(stored) : null;
  }
}
