import { Component, OnInit } from '@angular/core';
import { InboxService } from '../../../../services/inbox.service';
import { PriorityInboxService } from '../../../../services/priority-inbox.service';
import { Email } from '../../../../shared/models/email';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";
import { CommonModule } from '@angular/common';
import { TrashService } from '../../../../services/trash.service';
import { FormsModule } from '@angular/forms';
import { FolderService } from '../../../../services/folder';

@Component({
  selector: 'app-PriorityInbox',
  templateUrl: './PriorityInbox.component.html',
  styleUrls: ['./PriorityInbox.component.css'],
  imports: [EmailDisplayComponent, CommonModule, FormsModule],
})
export class PriorityInboxComponent implements OnInit {
  folders: string[] = [];
  emails: Email[] = [];
  selectedEmailIds: string[] = [];
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
    dateWithin: '1w'
  };

  constructor(
    private inboxService: InboxService,
    private priorityInboxService: PriorityInboxService,
    private trashService: TrashService,
    private folderService: FolderService ,
  ) {}

  ngOnInit(): void {
    this.loadEmails();
    this.loadFolders();
  }
  loadFolders(): void {
    this.folderService.getAllFolders().subscribe({
      next: (data) => { this.folders = data; },
      error: (err) => { 
        console.error('Failed to load folders:', err); 
        this.errorMessage = 'Could not load folder list.';
      }
    });
  }

  loadEmails(): void {
    this.isLoading = true;
    this.selectedEmail = null;

    // Try priority inbox service first, fallback to regular inbox with priority sorting
    this.priorityInboxService.getPriorityInboxEmails(this.currentPage, this.pageSize, 'PRIORITY')
      .subscribe({
        next: (response: any) => {
          this.emails = response.data;
          this.totalEmails = response.totalRecords;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Priority inbox service not available, using fallback:', err);
          // Fallback to inbox service with priority sorting
          this.inboxService.getInboxEmails(this.currentPage, this.pageSize, 'PRIORITY').subscribe({
            next: (response: any) => {
              this.emails = response.data;
              this.totalEmails = response.totalRecords;
              this.isLoading = false;
            },
            error: (fallbackErr) => {
              console.error('Fallback error:', fallbackErr);
              this.errorMessage = 'Failed to load priority emails';
              this.isLoading = false;
            }
          });
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
      this.priorityInboxService.searchPriorityInbox(this.searchQuery, this.currentPage, this.pageSize)
        .subscribe({
          next: (response: any) => {
            this.emails = response.data;
            this.totalEmails = response.totalRecords;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Priority search not available, using fallback:', err);
            // Fallback to regular inbox search
            this.inboxService.searchInbox(this.searchQuery, this.currentPage, this.pageSize)
              .subscribe({
                next: (response: any) => {
                  // Sort the results by priority
                  if (response.data) {
                    response.data.sort((a: any, b: any) => (b.priority || 0) - (a.priority || 0));
                  }
                  this.emails = response.data;
                  this.totalEmails = response.totalRecords;
                  this.isLoading = false;
                },
                error: (fallbackErr) => {
                  console.error('Fallback search error:', fallbackErr);
                  this.errorMessage = 'Failed to search priority emails';
                  this.isLoading = false;
                }
              });
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

    this.priorityInboxService.filterPriorityInbox(filters, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: any) => {
          this.emails = response.data;
          this.totalEmails = response.totalRecords;
          this.isLoading = false;
          this.closeFilterModal();
        },
        error: (err) => {
          console.error('Priority filter not available, using fallback:', err);
          // Fallback to regular inbox filter
          this.inboxService.filterInbox(filters, this.currentPage, this.pageSize)
            .subscribe({
              next: (response: any) => {
                // Sort the results by priority
                if (response.data) {
                  response.data.sort((a: any, b: any) => (b.priority || 0) - (a.priority || 0));
                }
                this.emails = response.data;
                this.totalEmails = response.totalRecords;
                this.isLoading = false;
                this.closeFilterModal();
              },
              error: (fallbackErr) => {
                console.error('Fallback filter error:', fallbackErr);
                this.errorMessage = 'Failed to apply filters';
                this.isLoading = false;
              }
            });
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
  toggleSelectEmail(emailId: string, event: Event) {
  event.stopPropagation();

  if (this.selectedEmailIds.includes(emailId)) {
    this.selectedEmailIds = this.selectedEmailIds.filter(id => id !== emailId);
  } else {
    this.selectedEmailIds.push(emailId);
  }
}
  stopProp(event: Event): void {
    event.stopPropagation();
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

