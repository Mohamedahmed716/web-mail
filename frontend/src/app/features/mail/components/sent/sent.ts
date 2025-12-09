import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../../core/services/api.service';
import { ComposeService } from '../../../../services/compose.service';
import { Email } from '../../../../shared/models/email';
import { User } from '../../../../shared/models/user';
import {HttpHeaders} from '@angular/common/http';

@Component({
  selector: 'app-sent',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sent.html',
  styleUrls: ['./sent.css'] // We will reuse the same CSS
})
export class Sent implements OnInit {

  sentEmails: Email[] = [];
  isLoading = true;

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

  // When clicking a sent email, we usually view it.
  // For now, we can re-use the Compose modal in "View Mode"
  // or simply log the action until you build a View Component.
  openEmail(email: Email) {
    console.log('Opening sent email:', email);
    // this.composeService.openCompose(email); // Uncomment if you want to open it in the popup
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }

  // --- Helpers for HTML ---

  getReceiversList(receivers: any[]): string {
    // Handle both String[] (new backend) and User[] (old model)
    if (!receivers || receivers.length === 0) return '(No Receivers)';
    if (typeof receivers[0] === 'string') {
      return receivers.join(', ');
    }
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
