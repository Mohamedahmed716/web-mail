import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../shared/models/user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private AUTH_API = 'http://localhost:8080/api/auth/';

  constructor(private http: HttpClient) { }

  login(user: User): Observable<any> {
    return this.http.post(this.AUTH_API + 'login', {
      email: user.email,
      password: user.password,
    });
  }

  register(user: User): Observable<any> {
    return this.http.post(this.AUTH_API + 'signup', {
      name: user.name,
      email: user.email,
      password: user.password,
      favoriteMovie: user.favoriteMovie, // Security question for password reset
    });
  }

  // Password Reset Flow Methods

  /**
   * Step 1: Verify if email exists in the system
   */
  verifyEmail(email: string): Observable<any> {
    return this.http.post(this.AUTH_API + 'verify-email', { email });
  }

  /**
   * Step 2: Verify security question answer (favorite movie)
   */
  verifySecurityQuestion(email: string, favoriteMovie: string): Observable<any> {
    return this.http.post(this.AUTH_API + 'verify-security-question', {
      email,
      favoriteMovie,
    });
  }

  /**
   * Step 3: Reset password after security verification
   */
  resetPassword(email: string, favoriteMovie: string, newPassword: string): Observable<any> {
    return this.http.post(this.AUTH_API + 'reset-password', {
      email,
      favoriteMovie,
      newPassword,
    });
  }
}
