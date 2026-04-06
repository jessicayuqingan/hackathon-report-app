import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Redirect to login, preserving intended URL
  router.navigate(['/login'], {
    queryParams: { redirect: state.url }
  });
  return false;
};

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.getCurrentUser();
  const requiredRoles = route.data && (route.data['roles'] as string[] | undefined);

  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  const userRole = user?.role || '';
  const allowed = requiredRoles.some(r => userRole.includes(r));

  if (allowed) {
    return true;
  }

  // Authenticated but not authorized for this route – send to default reports page
  router.navigate(['/reports']);
  return false;
};
