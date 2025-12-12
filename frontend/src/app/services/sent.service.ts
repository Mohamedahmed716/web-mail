import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SentService {
  private baseUrl = 'http://localhost:8080/api/send';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token') || '';
    return new HttpHeaders().set('Authorization', token);
  }

  /**
   * Get sent emails with pagination (new endpoint)
   */
  getSentEmails(page: number, pageSize: number, sortBy: string): Observable<any> {
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
   * Load sent emails (backward compatibility)
   */
  loadSent(): Observable<any> {
    return this.http.get(`${this.baseUrl}/loadSent`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Simple search in sent emails
   */
  searchSent(query: string, page: number = 1, pageSize: number = 10): Observable<any> {
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
  filterSent(filters: any, page: number = 1, pageSize: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.post(`${this.baseUrl}/filter`, filters, {
      headers: this.getHeaders(),
      params: params
    });
  }
}
