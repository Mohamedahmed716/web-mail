import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../../core/services/api.service';
import { ComposeService } from '../../../../services/compose.service';
import { Email } from '../../../../shared/models/email';
import { User } from '../../../../shared/models/user';
import {HttpHeaders} from '@angular/common/http';
import {EmailDisplayComponent} from '../EmailDisplay/EmailDisplay.component';

@Component({
  selector: 'app-sent',
  standalone: true,
  imports: [CommonModule, EmailDisplayComponent],
  templateUrl: './sent.html',
  styleUrls: ['./sent.css'] // We will reuse the same CSS
})
export class Sent implements OnInit {

  sentEmails: Email[] = [];
  isLoading = true;

  // 3. Track the selected email (null = List View, Object = Detail View)
  selectedEmail: Email | null = null;


  constructor(
    private apiService: ApiService,
    private composeService: ComposeService
  ) {}

  ngOnInit() {
    this.loadSentMails();

    this.composeService.refresh$.subscribe(() => {
      this.loadSentMails();
    });
  }

  loadSentMails() {
    this.isLoading = true;
    const token = localStorage.getItem('auth-token') || '';
    const headers = new HttpHeaders().set('Authorization', token);
    // Assumes your backend has @GetMapping("/loadSent") in SendController
    this.apiService.get('/send/loadSent', {headers}).subscribe({
      next: (data) => {
        this.sentEmails = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading sent mails:', error);
        this.isLoading = false;
      }
    });
  }

  openEmail(email: Email) {
    // Bridge Logic:
    // The Display Component looks at 'attachments[]'.
    // The Backend sends 'attachmentNames[]'.
    // We map the names to the attachments array if it's empty so the view works.
    if ((!email.attachments || email.attachments.length === 0) && email.attachmentNames) {
      // Cast to any because your model says File[] but we are putting strings in for the Display Component
      email.attachments = email.attachmentNames as any;
    }

    this.selectedEmail = email;
  }

  closeEmail() {
    this.selectedEmail = null;
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }

  // Helpers
  getReceiversList(receivers: any[]): string {
    if (!receivers || receivers.length === 0) return '(No Receivers)';
    if (typeof receivers[0] === 'string') return receivers.join(', ');
    return receivers.map((r: any) => r.email || r).join(', ');
  }

  getPriorityLabel(priority: number | undefined): string {
    if (!priority) return 'medium';
    switch (priority) {
      case 5: return 'Critical';
      case 4: return 'High';
      case 3: return 'Medium';
      case 2: return 'Low';
      case 1: return 'Very low';
      default: return 'medium';
    }
  }
}
