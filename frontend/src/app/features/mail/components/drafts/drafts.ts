import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComposeService } from '../../../../services/compose.service';
import { DraftsService } from '../../../../services/drafts.service';
import { Email } from '../../../../shared/models/email';
import { User } from '../../../../shared/models/user';
import { ApiService } from '../../../../core/services/api.service';
import {HttpHeaders, HttpParams} from '@angular/common/http';
import { TrashService } from '../../../../services/trash.service';
import { EmailDisplayComponent } from "../EmailDisplay/EmailDisplay.component";
import { FolderService } from '../../../../services/folder';

@Component({
  selector: 'app-drafts',
  standalone: true,
  imports: [CommonModule, FormsModule, EmailDisplayComponent],
  templateUrl: './drafts.html',
  styleUrls: ['./drafts.css']
})
export class Drafts implements OnInit {
  folders: string[] = [];
  selectedEmailIds: string[] = [];
  drafts: Email[] = [];
  emails: Email[] = [];
  currentPage: number = 1;
  pageSize: number = 10;
  isLoading = true;
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
    private composeService: ComposeService, 
    private apiService: ApiService,
    private draftsService: DraftsService,
    private trashService: TrashService,
    private folderService: FolderService ,
  ) {}


  ngOnInit() {
    this.loadEmails();

    this.composeService.refresh$.subscribe(() => {
      this.loadEmails();
    });
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

    this.draftsService.getDraftEmails(this.currentPage, this.pageSize, 'DATE')
      .subscribe({
        next: (response: any) => {
          this.emails = response.data || response;
          this.drafts = this.emails; // Keep backward compatibility
          this.totalEmails = response.totalRecords || this.emails.length;
          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          // Fallback to old API
          this.loadDraftsLegacy();
        }
      });
  }

  loadDraftsLegacy() {
    const token = localStorage.getItem('auth-token') || '';
    const headers = new HttpHeaders().set('Authorization', token);
    
    this.apiService.get('/draft/loadDrafts', { headers }).subscribe({
      next: (data: Email[]) => {
        this.drafts = data;
        this.emails = data;
        this.totalEmails = data.length;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading drafts:', error);
        this.errorMessage = 'Failed to load drafts';
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
      this.draftsService.searchDrafts(this.searchQuery, this.currentPage, this.pageSize)
        .subscribe({
          next: (response: any) => {
            this.emails = response.data || response;
            this.drafts = this.emails;
            this.totalEmails = response.totalRecords || this.emails.length;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Search error:', err);
            this.errorMessage = 'Failed to search drafts';
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

    this.draftsService.filterDrafts(filters, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: any) => {
          this.emails = response.data || response;
          this.drafts = this.emails;
          this.totalEmails = response.totalRecords || this.emails.length;
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

  openDraft(draft: Email) {
    this.composeService.openCompose(draft);
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }

  getReceiversList(receivers: any[]): string {
    // Handle both String[] (new backend) and User[] (old model)
    if (!receivers || receivers.length === 0) return '(No Receivers)';
    if (typeof receivers[0] === 'string') {
      return receivers.join(', ');
    }
    return receivers.map((r: any) => r.email || r).join(', ');
  }

  getPriorityLabel(priority: number | undefined): string {
    if(!priority) {
      return 'medium';
    }

    switch (priority) {
      case 5: return 'Critical';
      case 4: return 'High';
      case 3: return 'Medium';
      case 2: return 'Low';
      case 1: return 'Very low';
      default: return 'medium';
    }
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

  const folder = "drafts";

  this.selectedEmailIds.forEach(id => {
    this.trashService.moveToTrash(id, folder).subscribe(() => {
      this.loadEmails();
    });
  });

  this.selectedEmailIds = [];
}
toggleSelectAll(event: any) {
  if (event.target.checked) {
    this.selectedEmailIds = this.drafts.map(e => e.id);
  } else {
    this.selectedEmailIds = [];
  }
}

  get totalPages(): number {
    if (this.totalEmails === 0) return 1;
    return Math.ceil(this.totalEmails / this.pageSize);
  }
moveSelectedEmails(targetFolder: string): void {
    if (this.selectedEmailIds.length === 0) return;

    // --- 1. SETUP ---
    const totalMoves = this.selectedEmailIds.length;
    let successfulMoves = 0;
    let failed = false;
    const sourceFolder = "drafts";
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
