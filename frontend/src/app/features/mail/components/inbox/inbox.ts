import { Component,OnInit } from '@angular/core';
import { InboxService } from '../../../../services/inbox.service';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";

@Component({
  selector: 'app-inbox',
  imports: [CommonModule, EmailDisplayComponent],
  templateUrl: './inbox.html',
  styleUrl: './inbox.css',

})
export class Inbox implements OnInit  {

  emails: Email[] = [];
  currentPage: number = 1;
  pageSize: number = 10;
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedEmail: Email | null = null;


  constructor(private inboxService: InboxService) { }



  ngOnInit(): void {

    this.loadEmails();
    
  }
  loadEmails(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.inboxService.getInboxEmails(this.currentPage, this.pageSize, 'DATE')
      .subscribe({
        next: (data: any[]) => { 
          this.emails = data;
          this.isLoading = false;
          console.log('Emails loaded:', this.emails); // debugging
        },
        error: (err) => {
          console.error('Error fetching emails:', err);
          this.isLoading = false;
          if (err.status === 401) {
            this.errorMessage = 'Session expired. Please login again.';
          } else {
            this.errorMessage = 'Failed to load emails from server.';
          }
        }
      });
  }

  changePage(step: number): void {
    this.currentPage += step;
    this.loadEmails();
  }
  selectEmail(email: Email): void {
    this.selectedEmail = email;
  }
}