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
  totalEmails: number=0;


  constructor(private inboxService: InboxService) { }



  ngOnInit(): void {

    this.loadEmails();
    
  }
 loadEmails(): void {
    this.isLoading = true;
    this.selectedEmail = null;

    this.inboxService.getInboxEmails(this.currentPage, this.pageSize, 'DATE')
      .subscribe({
        next: (response: any) => {
          
          this.emails = response.data;         
          this.totalEmails = response.totalRecords; 

          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Failed to load emails';
          this.isLoading = false;
        }
      });
  }

  changePage(step: number): void {
    const maxPages = Math.ceil(this.totalEmails / this.pageSize);
    const nextPage = this.currentPage + step;

    if (nextPage >= 1 && nextPage <= maxPages) {
      this.currentPage = nextPage;
      this.loadEmails();
    }
  }
  selectEmail(email: Email): void {
    this.selectedEmail = email;
  }
get totalPages(): number {
  if (this.totalEmails === 0) return 1;
  return Math.ceil(this.totalEmails / this.pageSize);
}
}