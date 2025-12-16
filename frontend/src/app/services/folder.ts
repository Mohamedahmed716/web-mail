import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FolderService {
  
  // Base path used for all folder management and content retrieval
  private base = 'http://localhost:8080/api/folders'; 

  constructor(private http: HttpClient) {}

  private auth() {
    const token = localStorage.getItem('auth-token') || '';
    return { headers: new HttpHeaders().set('Authorization', token) };
  }

  // --- Folder Metadata Operations ---
  
  // GET: /api/folders (Retrieves list of folder names)
  getAllFolders(): Observable<string[]> {
    return this.http.get<string[]>(this.base, this.auth());
  }

  // POST: /api/folders (Creates a new folder)
  createFolder(folderName: string): Observable<string> {
    return this.http.post(
      this.base, 
      { name: folderName }, 
      { 
          ...this.auth(), 
          responseType: 'text' 
      }
    ) as Observable<string>; 
  }

  // PUT: /api/folders/{oldName} (Renames a folder)
  renameFolder(oldName: string, newName: string): Observable<string> {
  return this.http.put(
    `${this.base}/${oldName}`, 
    { newName: newName }, 
    { 
      ...this.auth(), 
      // Add this line to handle non-JSON string responses from the server
      responseType: 'text' 
    }
  ) as Observable<string>;
}

  // DELETE: /api/folders/{folderName} (Deletes a folder)
  deleteFolder(folderName: string): Observable<any> {
    return this.http.delete(`${this.base}/${folderName}`, this.auth());
  }
  
  // --- Folder Content Retrieval ---
  
  /**
   * Fetches the list of emails for a specific folder (Inbox, Sent, Custom, etc.)
   * Assumes the backend uses the path: GET /api/folders/{folderName}
   */
  loadMailsByFolder(folderName: string, page: number = 1, pageSize: number = 50): Observable<any[]> {
    let params = new HttpParams()
        .set('page', page.toString())
        .set('pageSize', pageSize.toString());

    return this.http.get<any[]>(`${this.base}/${folderName}`, { 
        ...this.auth(), 
        params: params 
    });
  }

  
  // POST: /api/folders/move/{mailId}/{targetFolder}?sourceFolder={sourceFolder}
  singleMoveEmail(mailId: string, sourceFolder: string, targetFolder: string): Observable<any> {
    const params = { sourceFolder: sourceFolder };
    
    return this.http.post(`${this.base}/move/${mailId}/${targetFolder}`, null, { 
        ...this.auth(), 
        params: params 
    });
  }
  returnToOriginalFolder(mailId: string, sourceFolder: string,): Observable<any> {
    const params = { sourceFolder: sourceFolder };
    
    return this.http.post(`${this.base}/return/${mailId}`, null, { 
        ...this.auth(), 
        params: params 
    });
  }
  
}