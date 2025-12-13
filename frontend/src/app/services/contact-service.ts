import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StorageService } from '../core/services/storage.service';

export interface Contact {
  id?: string;
  name: string;
  emails: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = 'http://localhost:8080/api/contacts';

  constructor(
    private http: HttpClient,
    private storageService: StorageService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.storageService.getToken() || '';

    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token
    });
  }

  getAllContacts(): Observable<Contact[]> {
    return this.http.get<Contact[]>(this.apiUrl, {
      headers: this.getHeaders()
    });
  }

  addContact(contact: Contact): Observable<Contact> {
    return this.http.post<Contact>(this.apiUrl, contact, {
      headers: this.getHeaders()
    });
  }

  updateContact(id: string, contact: Contact): Observable<Contact> {
    return this.http.put<Contact>(`${this.apiUrl}/${id}`, contact, {
      headers: this.getHeaders()
    });
  }

  deleteContact(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  searchContacts(query: string, searchType: string = 'default'): Observable<Contact[]> {
    const params = new HttpParams()
      .set('query', query)
      .set('searchType', searchType);

    return this.http.get<Contact[]>(`${this.apiUrl}/search`, {
      params,
      headers: this.getHeaders()
    });
  }

  searchContactsByName(name: string): Observable<Contact[]> {
    const params = new HttpParams().set('name', name);

    return this.http.get<Contact[]>(`${this.apiUrl}/search/name`, {
      params,
      headers: this.getHeaders()
    });
  }

  searchContactsByEmail(email: string): Observable<Contact[]> {
    const params = new HttpParams().set('email', email);

    return this.http.get<Contact[]>(`${this.apiUrl}/search/email`, {
      params,
      headers: this.getHeaders()
    });
  }
}
