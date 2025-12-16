import { Component, OnInit } from '@angular/core';
import { InboxService } from '../../../../services/inbox.service';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";
import { TrashService } from '../../../../services/trash.service';
import { FolderService } from '../../../../services/folder';
@Component({
  selector: 'app-inbox',
  imports: [CommonModule, FormsModule, EmailDisplayComponent],
  templateUrl: './inbox.html',
  styleUrls: ['./inbox.css'],
})
export class Inbox implements OnInit {
  folders: string[] = [];
  selectedEmailIds: string[] = [];
  emails: Email[] = [];
  currentPage: number = 1;
  pageSize: number = 10;
  isLoading: boolean = false;
  errorMessage: string | null = null;
  selectedEmail: Email | null = null;
  totalEmails: number = 0;

  // Search functionality
  searchQuery: string = '';
  private searchTimeout: any;

  // Filter modal
  showFilterModal: boolean = false;
  filterCriteria = {
    from: '',
    to: '',
    subject: '',
    hasWords: '',
    doesntHave: '',
    dateWithin: '1w',
    hasAttachment: ''
  };

  sortAttribute: string = 'DATE';
  isAscending: boolean = false;


  constructor(private inboxService: InboxService,
    private trashService: TrashService,
    private folderService: FolderService,
  ) { }

  ngOnInit(): void {
    this.loadEmails();
    this.loadFolders();
  }

  sortAttributes = [
    { label: 'Date', value: 'DATE' },
    { label: 'Priority', value: 'PRIORITY' },
    { label: 'Sender', value: 'SENDER' },
    { label: 'Subject', value: 'SUBJECT' },
  ];

  toggleDirection() {
    this.isAscending = !this.isAscending;
    this.loadEmails();
  }

  onAttributeChange() {
    this.currentPage = 1;
    this.loadEmails();
  }

  getBackendSortString(): string {
    switch (this.sortAttribute) {
      case 'DATE':
        return this.isAscending ? 'DATE_OLDEST' : 'DATE_NEWEST';

      case 'PRIORITY':


        return this.isAscending ? 'PRIORITY_LOW' : 'PRIORITY_HIGH';

      case 'SENDER':
        return this.isAscending ? 'SENDER_ASC' : 'SENDER_DESC';

      case 'SUBJECT':
        return this.isAscending ? 'SUBJECT_ASC' : 'SUBJECT_DESC';

      default:
        return 'DATE_NEWEST';
    }
  }




   private SYSTEM_FOLDERS = [
  'Inbox', 'Sent', 'Drafts', 'Trash', 'Priority Inbox', 'Contacts'
];

loadFolders(): void {
  this.folderService.getAllFolders().subscribe({
    next: (data) => {
      // Create a lowercase version of system folders for safe comparison
      const systemFoldersLower = this.SYSTEM_FOLDERS.map(f => f.toLowerCase());

      // Filter the data: only keep folders NOT in the system list
      this.folders = data.filter(folderName => 
        !systemFoldersLower.includes(folderName.toLowerCase())
      );
    },
    error: (err) => { 
      console.error('Failed to load folders:', err); 
      this.errorMessage = 'Could not load folder list.';
    }
  });
}
  loadEmails(): void {
    this.isLoading = true;
    this.selectedEmail = null;
    const sortParam = this.getBackendSortString();

    this.inboxService.getInboxEmails(this.currentPage, this.pageSize, sortParam)
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

    
    if (!email.isRead) {

      email.isRead = true;


      this.inboxService.markAsRead(email.id).subscribe({
        next: () => {
          console.log('Email marked as read on server');
        },
        error: (err) => {
          console.error('Failed to mark email as read:', err);

          email.isRead = false;
        }
      });
    }
  }
  // Search functionality with debouncing
  onSearchInput(): void {
    // Clear previous timeout
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    // Set new timeout for debouncing (300ms delay)
    this.searchTimeout = setTimeout(() => {
      this.onSearch();
    }, 300);
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.isLoading = true;
      this.inboxService.searchInbox(this.searchQuery, this.currentPage, this.pageSize)
        .subscribe({
          next: (response: any) => {
            this.emails = response.data;
            this.totalEmails = response.totalRecords;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Search error:', err);
            this.errorMessage = 'Failed to search emails';
            this.isLoading = false;
          }
        });
    } else {
      this.loadEmails();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    // Clear any pending search timeout
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    this.loadEmails();
  }

  openFilterModal(): void {
    this.showFilterModal = true;
  }

  closeFilterModal(): void {
    this.showFilterModal = false;
  }

  applyFilter(): void {
    this.isLoading = true;

    const filters: any = {};

    if (this.filterCriteria.from) filters.from = this.filterCriteria.from;
    if (this.filterCriteria.to) filters.to = this.filterCriteria.to;
    if (this.filterCriteria.subject) filters.subject = this.filterCriteria.subject;
    if (this.filterCriteria.hasWords) filters.hasWords = this.filterCriteria.hasWords;
    if (this.filterCriteria.doesntHave) filters.doesntHave = this.filterCriteria.doesntHave;
    if (this.filterCriteria.dateWithin) filters.dateWithin = this.filterCriteria.dateWithin;
    if (this.filterCriteria.hasAttachment) filters.hasAttachment = this.filterCriteria.hasAttachment;

    this.inboxService.filterInbox(filters, this.currentPage, this.pageSize)
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
      dateWithin: '1w',
      hasAttachment: ''
    };
  }

  get totalPages(): number {
    if (this.totalEmails === 0) return 1;
    return Math.ceil(this.totalEmails / this.pageSize);
  }

  stopProp(event: Event): void {
    event.stopPropagation();
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
  moveSelectedEmails(targetFolder: string): void {
    if (this.selectedEmailIds.length === 0) return;

    // --- 1. SETUP ---
    const totalMoves = this.selectedEmailIds.length;
    let successfulMoves = 0;
    let failed = false;
    const sourceFolder = "inbox";
    const selectedEmailIdToMove = this.selectedEmail?.id;
    this.selectedEmail = null;
    this.errorMessage = null;

    // --- 2. Iterate Over All Selected IDs ---
    this.selectedEmailIds.forEach(idToMove => {

      // **CRITICAL FIX:** Pass the specific ID from the loop (idToMove)
      this.folderService.singleMoveEmail(idToMove, sourceFolder, targetFolder).subscribe({
        next: () => {
          successfulMoves++;

          // 3. CHECK COMPLETION (Runs on every success)
          if (successfulMoves === totalMoves) {
            // All calls finished successfully
            this.loadEmails();
          }
        },
        error: (err) => {
          // Set flag to stop any subsequent refreshes if a failure occurs
          if (!failed) {
            this.errorMessage = `Failed to move some emails. Check connection or folder access.`;
            this.isLoading = false; // Stop loading immediately on first error
          }
          failed = true;
          console.error(`Failed to move email ${idToMove}:`, err);

          // If the error happens on the very last move, we still need to refresh
          if (successfulMoves + 1 === totalMoves) {
            // Since we already set failed=true, finishOperation will just refresh.
            this.loadEmails();
          }
        }
      });
    });

    // Clear selection immediately for a clean UI state
    this.selectedEmailIds = [];
  }
  handleMoveSelectionChange(event: Event): void {

    // CRITICAL FIX: Cast the event target to HTMLSelectElement
    const target = event.target as HTMLSelectElement;
    const targetFolder = target.value;

    // Reset the select box immediately to allow re-selection
    target.value = '';

    if (targetFolder) {
      // You can add confirmation here if needed

      // Call the multi-move logic you developed earlier
      this.moveSelectedEmails(targetFolder);
    }
  }


}