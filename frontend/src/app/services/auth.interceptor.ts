import { HttpInterceptorFn } from '@angular/common/http';

const API_PREFIX = 'http://localhost:8080/api';
const TOKEN_KEY = 'auth_token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(TOKEN_KEY);

  // 只对调用后端 API 的请求加 Authorization 头
  if (token && req.url.startsWith(API_PREFIX)) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  return next(req);
};
