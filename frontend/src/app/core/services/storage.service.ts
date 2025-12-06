import { Injectable } from '@angular/core';
import { User } from '../../shared/models/user';

@Injectable({
  providedIn: 'root',
})
export class StorageService {
  private USER_KEY = 'auth-user';
  private TOKEN_KEY = 'auth-token';

  constructor() {}

  public clean(): void {
    window.localStorage.clear();
  }

  public saveUser(user: User): void {
    window.localStorage.removeItem(this.USER_KEY);
    window.localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  public getUser(): User | null {
    const user = window.localStorage.getItem(this.USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return null;
  }

  public saveToken(token: string): void {
    window.localStorage.removeItem(this.TOKEN_KEY);
    window.localStorage.setItem(this.TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return window.localStorage.getItem(this.TOKEN_KEY);
  }

  public isLoggedIn(): boolean {
    const token = window.localStorage.getItem(this.TOKEN_KEY);
    return !!token; //Converts any value to boolean
  }
}
