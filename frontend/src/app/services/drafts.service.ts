import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DraftsService {
  private baseUrl = 'http://localhost:8080/api/draft';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token') || '';
    return new HttpHeaders().set('Authorization', token);
  }

  /**
   * Get draft emails with pagination
   */
  getDraftEmails(page: number, pageSize: number, sortBy: string): Observable<any> {
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
   * Load drafts (backward compatibility)
   */
  loadDrafts(): Observable<any> {
    return this.http.get(`${this.baseUrl}/loadDrafts`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Simple search in drafts
   */
  searchDrafts(query: string, page: number = 1, pageSize: number = 10): Observable<any> {
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
  filterDrafts(filters: any, page: number = 1, pageSize: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', pageSize.toString());

    return this.http.post(`${this.baseUrl}/filter`, filters, {
      headers: this.getHeaders(),
      params: params
    });
  }
}