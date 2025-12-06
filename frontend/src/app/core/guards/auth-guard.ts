import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '../services/storage.service';

export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const token = storage.getToken();

  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `${token}`,
      },
    });
    return next(clonedRequest);
  }
  return next(req);
};
