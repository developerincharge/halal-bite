// src/app/core/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Auth Guard — blocks unauthenticated users from protected routes.
 * If the user isn't logged in, redirects them to /login.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};

/**
 * Owner Guard — ensures only RESTAURANT_OWNER role can access.
 * Redirects non-owners to login.
 */
export const ownerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isOwner()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
