import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Email } from '../shared/models/email';

@Injectable({ providedIn: 'root' })
export class TrashService {

  private base = 'http://localhost:8080/api/trash';

  constructor(private http: HttpClient) {}

  private auth() {
    const token = localStorage.getItem('auth-token') || '';
    return { headers: new HttpHeaders().set('Authorization', token) };
  }

  getTrash(): Observable<Email[]> {
    return this.http.get<Email[]>(this.base, this.auth());
  }

  moveToTrash(mailId: string, fromFolder: string) {
    return this.http.post(`${this.base}/move/${mailId}/${fromFolder}`, {}, this.auth());
  }

  restore(mailId: string) {
    return this.http.post(`${this.base}/restore/${mailId}`, {}, this.auth());
  }

  deleteForever(mailId: string) {
    return this.http.delete(`${this.base}/deleteForever/${mailId}`, this.auth());
  }
  bulkMoveToTrash(mailIds: string[], fromFolder: string) {
    // Sends the list of IDs in the request body to the new backend endpoint
    return this.http.post(
      `${this.base}/bulk/move/${fromFolder}`, 
      mailIds, 
      this.auth()
    );
  }
  bulkRestore(mailIds: string[]) {
    return this.http.post(`${this.base}/bulk/restore`, mailIds, this.auth());
  }

  // NEW: Bulk delete forever service method
  bulkDeleteForever(mailIds: string[]) {
    return this.http.delete(`${this.base}/bulk/deleteForever`, {
        body: mailIds, // DELETE request needs the body passed this way
        ...this.auth() 
    });
  }
}
