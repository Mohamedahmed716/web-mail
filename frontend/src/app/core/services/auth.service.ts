import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../shared/models/user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private AUTH_API = 'http://localhost:8080/api/auth/';

  constructor(private http: HttpClient) {}

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
    });
  }
}
