import { Component, OnInit } from '@angular/core';
import { TrashService } from '../../../../services/trash.service';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";

@Component({
  selector: 'app-trash',
  imports: [CommonModule, FormsModule, EmailDisplayComponent],
  templateUrl: './trash.html',
  styleUrl: './trash.css',
})
export class Trash implements OnInit {

  emails: Email[] = [];
  currentPage: number = 1;
  pageSize: number = 10;
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedEmail: Email | null = null;
  totalEmails: number = 0;

  // Search functionality
  searchQuery: string = '';

  // Filter modal
  showFilterModal: boolean = false;
  filterCriteria = {
    from: '',
    to: '',
    subject: '',
    hasWords: '',
    doesntHave: '',
    dateWithin: '1w'
  };

  constructor(private trashService: TrashService) { }

  ngOnInit(): void {
    this.loadEmails();
  }

  loadEmails(): void {
    this.isLoading = true;
    this.selectedEmail = null;

    this.trashService.getTrashEmails(this.currentPage, this.pageSize, 'DATE')
      .subscribe({
        next: (response: any) => {
          this.emails = response.data;
          this.totalEmails = response.totalRecords;
          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Failed to load trash emails';
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

  // Search functionality
  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.isLoading = true;
      this.trashService.searchTrash(this.searchQuery, this.currentPage, this.pageSize)
        .subscribe({
          next: (response: any) => {
            this.emails = response.data;
            this.totalEmails = response.totalRecords;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Search error:', err);
            this.errorMessage = 'Failed to search trash';
            this.isLoading = false;
          }
        });
    } else {
      this.loadEmails();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadEmails();
  }

  // Filter functionality
  openFilterModal(): void {
    this.showFilterModal = true;
  }

  closeFilterModal(): void {
    this.showFilterModal = false;
  }

  applyFilter(): void {
    this.isLoading = true;

    // Build filter object (only send non-empty values)
    const filters: any = {};

    if (this.filterCriteria.from) filters.from = this.filterCriteria.from;
    if (this.filterCriteria.to) filters.to = this.filterCriteria.to;
    if (this.filterCriteria.subject) filters.subject = this.filterCriteria.subject;
    if (this.filterCriteria.hasWords) filters.hasWords = this.filterCriteria.hasWords;
    if (this.filterCriteria.doesntHave) filters.doesntHave = this.filterCriteria.doesntHave;
    if (this.filterCriteria.dateWithin) filters.dateWithin = this.filterCriteria.dateWithin;

    this.trashService.filterTrash(filters, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: any) => {
          this.emails = response.data;
          this.totalEmails = response.totalRecords;
          this.isLoading = false;
          this.closeFilterModal();
        },
        error: (err) => {
          console.error('Filter error:', err);
          this.errorMessage = 'Failed to apply filters';
          this.isLoading = false;
        }
      });
  }

  clearFilter(): void {
    this.filterCriteria = {
      from: '',
      to: '',
      subject: '',
      hasWords: '',
      doesntHave: '',
      dateWithin: '1w'
    };
  }

  get totalPages(): number {
    if (this.totalEmails === 0) return 1;
    return Math.ceil(this.totalEmails / this.pageSize);
  }

  stopProp(event: Event): void {
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
      case 5: return 'critical';
      case 4: return 'high';
      case 3: return 'medium';
      case 2: return 'low';
      case 1: return 'low';
      default: return 'medium';
    }
  }
}
