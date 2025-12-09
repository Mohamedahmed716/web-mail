import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Mail } from '../features/mail/mail';

@Injectable({
  providedIn: 'root',
})
export class InboxService {
  private apiUrl = 'http://localhost:8080/api/inbox';

  constructor(private httpclient: HttpClient) {}
  getInboxEmails(page: number, size: number, sort: string) {
    const token = localStorage.getItem('auth-token') || '';

    const headers = new HttpHeaders().set('Authorization', token);
    const params = {
      page: page.toString(),
      size: size.toString(),
      sort: sort,
    };
    return this.httpclient.get<Mail[]>(this.apiUrl, { headers, params });
  }
}
