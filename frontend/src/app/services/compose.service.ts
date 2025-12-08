import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import {Email} from '../shared/models/email';


@Injectable({
  providedIn: 'root'
})
export class ComposeService {
  private isOpenSource = new BehaviorSubject<boolean>(false);
  private refreshSource = new Subject<void>();
  refresh$ = this.refreshSource.asObservable();
  isOpen$ = this.isOpenSource.asObservable();

  private draftSource = new BehaviorSubject<Email | null>(null);
  currentDraft$ = this.draftSource.asObservable();

  constructor() {}

  notifyRefresh() {
    this.refreshSource.next();
  }

  openCompose(draft: Email | null = null) {
    this.draftSource.next(draft);
    this.isOpenSource.next(true);
  }

  closeCompose() {
    this.isOpenSource.next(false);
  }
}
