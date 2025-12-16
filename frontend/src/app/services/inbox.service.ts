import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InboxService {
  private baseUrl = 'http://localhost:8080/api/inbox';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token') || '';
    return new HttpHeaders().set('Authorization', token);
  }

  /**
   * Get inbox emails with pagination
   */
  getInboxEmails(page: number, pageSize: number, sortBy: string): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString())
      .set('sort', sortBy);

    return this.http.get(`${this.baseUrl}`, {
      headers: this.getHeaders(),
      params: params
    });
  }

  /**
   * Simple search in inbox
   */
  searchInbox(query: string, page: number = 1, pageSize: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.get(`${this.baseUrl}/search`, {
      headers: this.getHeaders(),
      params: params
    });
  }

  /**
   * Advanced filter with multiple criteria
   */
  filterInbox(filters: any, page: number = 1, pageSize: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.post(`${this.baseUrl}/filter`, filters, {
      headers: this.getHeaders(),
      params: params
    });
  }

  /**
   * Mark an email as read
   */
  markAsRead(mailId: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${mailId}/read`, {}, {
      headers: this.getHeaders()
    });
  }
}
