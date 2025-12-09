import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Email } from "../../shared/models/email";

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  // The base URL for your Spring Boot Backend
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {
  }

  // Generic GET request
  get(path: string, options: { headers?: HttpHeaders, params?: HttpParams } = {}): Observable<any> {
    return this.http.get(`${this.baseUrl}${path}`, options);
  }

  // Generic POST request
  post<T>(path: string, body: Object = {}, options: { headers?: HttpHeaders, params?: HttpParams } = {}): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${path}`, body, options);
  }

  // Generic PUT request
  put(path: string, body: Object = {}): Observable<any> {
    return this.http.put(`${this.baseUrl}${path}`, body);
  }

  // Generic DELETE request
  delete(path: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}${path}`);
  }
}
