import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface EmailDraft {
  to: string;
  subject: string;
  body: string;
  priority: 'low' | 'medium' | 'high';
}

@Injectable({
  providedIn: 'root'
})
export class ComposeService {
  private isOpenSource = new BehaviorSubject<boolean>(false);
  isOpen$ = this.isOpenSource.asObservable();

  // Controls the data inside the form
  private draftSource = new BehaviorSubject<EmailDraft | null>(null);
  currentDraft$ = this.draftSource.asObservable();

  constructor() {}

  openCompose(draft: any | null = null) {
    this.draftSource.next(draft);
    this.isOpenSource.next(true);
  }

  closeCompose() {
    this.isOpenSource.next(false);
  }
}
