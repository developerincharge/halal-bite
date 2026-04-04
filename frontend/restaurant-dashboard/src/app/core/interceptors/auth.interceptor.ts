// src/app/core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * JWT Auth Interceptor
 *
 * Automatically attaches the Bearer token to every outgoing HTTP request.
 * This means you never have to manually add Authorization headers
 * in your services — the interceptor handles it globally.
 *
 * Also handles 401 responses by logging out and redirecting to login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Clone the request and add Authorization header if token exists
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expired or invalid — log out and redirect
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
