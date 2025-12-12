import { Component, OnInit } from '@angular/core';
import { InboxService } from '../../../../services/inbox.service';
import { Email } from '../../../../shared/models/email';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";
import { CommonModule } from '@angular/common';
import { TrashService } from '../../../../services/trash.service';
@Component({
  selector: 'app-PriorityInbox',
  templateUrl: './PriorityInbox.component.html',
  styleUrls: ['./PriorityInbox.component.css'],
  imports: [EmailDisplayComponent, CommonModule],
})
export class PriorityInboxComponent implements OnInit {
  emails: Email[] = [];
  selectedEmailIds: string[] = [];
  currentPage: number = 1;
  pageSize: number = 10;
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedEmail: Email | null = null;
  totalEmails: number = 0;

  constructor(private inboxService: InboxService,
    private trashService: TrashService,
  ) {}

  ngOnInit(): void {
    this.loadEmails();
  }
  loadEmails(): void {
    this.isLoading = true;
    this.selectedEmail = null;

    this.inboxService.getInboxEmails(this.currentPage, this.pageSize, 'PRIORITY').subscribe({
      next: (response: any) => {
        this.emails = response.data;
        this.totalEmails = response.totalRecords;

        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load emails';
        this.isLoading = false;
      },
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
  toggleSelectEmail(emailId: string, event: Event) {
  event.stopPropagation();

  if (this.selectedEmailIds.includes(emailId)) {
    this.selectedEmailIds = this.selectedEmailIds.filter(id => id !== emailId);
  } else {
    this.selectedEmailIds.push(emailId);
  }
}

deleteSelected() {
  if (this.selectedEmailIds.length === 0) return;

  const folder = "inbox";

  this.selectedEmailIds.forEach(id => {
    this.trashService.moveToTrash(id, folder).subscribe(() => {
      this.loadEmails();
    });
  });

  this.selectedEmailIds = [];
}
toggleSelectAll(event: any) {
  if (event.target.checked) {
    this.selectedEmailIds = this.emails.map(e => e.id);
  } else {
    this.selectedEmailIds = [];
  }
}

}

