import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const paraApi = req.url.startsWith(environment.apiUrl);
  const ehLogin = req.url.endsWith('/auth/login');

  const r = token && paraApi && !ehLogin ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(r).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && paraApi && !ehLogin) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
