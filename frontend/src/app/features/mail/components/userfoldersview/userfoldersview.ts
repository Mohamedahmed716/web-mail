import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // <-- Required for [(ngModel)]
import { Subscription } from 'rxjs';
import { FolderService } from '../../../../services/folder'; 
import { TrashService } from '../../../../services/trash.service';


@Component({
  selector: 'app-mailbox-view',
  standalone: true,
  // ADD FormsModule for ngModel, and EmailDisplayComponent when ready
  imports: [CommonModule, RouterModule, FormsModule], 
  templateUrl: './userfoldersview.html',
  styleUrls: ['./userfoldersview.css']
})
export class UserFoldersView implements OnInit, OnDestroy {
  folders: string[] = []; 
  // --- Core Folder State (Existing) ---
  currentFolderName: string = '';
  emails: any[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  private routeSubscription!: Subscription;
  
  // --- List/Toolbar State (New) ---
  selectedEmail: any | null = null;
  selectedEmailIds: string[] = [];
  searchQuery: string = '';
  
  // --- Pagination State (New) ---
  currentPage: number = 1;
  pageSize: number = 20; // Default items per page
  totalEmails: number = 0; // Total count from API (needs backend support)
  totalPages: number = 1;
  
  // --- Filtering State (New) ---
  showFilterModal: boolean = false;
  // Initialize filter criteria with default values
  filterCriteria = {
    from: '',
    to: '',
    subject: '',
    hasWords: '',
    doesntHave: '',
    dateWithin: '1w', // Default
    hasAttachment: ''
  };

  constructor(
    private route: ActivatedRoute,
    private folderService: FolderService ,
    private trashService: TrashService,
  ) {}

  ngOnInit(): void {
    // Subscribe to queryParamMap (e.g., ?f=inbox)
    this.routeSubscription = this.route.queryParamMap.subscribe(params => {
      const folderName = params.get('f'); 
      
      if (folderName) {
        this.currentFolderName = this.capitalize(folderName);
        // Reset state when folder changes
        this.resetState();
        this.loadEmailsForFolder(folderName);
      }
      this.loadFolders();
    });  
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  private resetState(): void {
    this.selectedEmail = null;
    this.selectedEmailIds = [];
    this.currentPage = 1;
    this.totalEmails = 0;
    this.totalPages = 1;
  }
  
  // --- Core Data Loading ---
  loadFolders(): void {
    this.folderService.getAllFolders().subscribe({
      next: (data) => { this.folders = data; },
      error: (err) => { 
        console.error('Failed to load folders:', err); 
        this.errorMessage = 'Could not load folder list.';
      }
    });
  }
  loadEmailsForFolder(folderName: string): void {
    this.isLoading = true; 
    this.errorMessage = null;

    // Use current page and page size for the request
    this.folderService.loadMailsByFolder(folderName, this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        // NOTE: If your API provides total count, capture it here:
        // this.totalEmails = data.totalCount; 
        // this.emails = data.mails;
        
        this.emails = data; // Assuming data is the mail list array
        this.totalEmails = data.length; // Temporary: treat list size as total
        this.totalPages = Math.ceil(this.totalEmails / this.pageSize); // Calculate total pages
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error('API Failed:', err);
        this.errorMessage = `Could not load emails. Error: ${err.statusText || 'Connection failed'}`;
        this.emails = []; 
        this.isLoading = false;
      }
    });
  }
  
  // --- List Item Actions ---

  selectEmail(email: any): void {
    this.selectedEmail = email;
    // You might add logic here to mark the email as 'read'
  }

  closeEmail(): void {
    this.selectedEmail = null;
  }
  
  // Helper to prevent the row click from propagating when checking a box
  stopProp(event: Event): void {
    event.stopPropagation();
  }

  // Helper to join receivers list for display in the 'To:' field
  getReceiversList(receivers: string[]): string {
      // Handles case where receivers might be null or undefined
      if (!receivers || receivers.length === 0) return 'Undisclosed Recipients'; 
      if (receivers.length === 1) return receivers[0];
      return `${receivers[0]} (+${receivers.length - 1} more)`;
  }
  
  // --- Selection Actions ---

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

  const folder = this.currentFolderName;

  this.selectedEmailIds.forEach(id => {
    this.trashService.moveToTrash(id, folder).subscribe(() => {
      this.loadEmailsForFolder(folder);
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
    const sourceFolder = this.currentFolderName.toLowerCase();
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
                    this.loadEmailsForFolder(sourceFolder);
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
                    this.loadEmailsForFolder(sourceFolder); 
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

  // --- Toolbar & Search Actions ---
  
  onSearch(): void {
    this.currentPage = 1;
    // TODO: Modify loadEmailsForFolder to pass this.searchQuery to the API
    this.loadEmailsForFolder(this.currentFolderName.toLowerCase());
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.onSearch(); // Reload without search query
  }
  
  // --- Filter Modal Actions ---

  openFilterModal(): void {
    this.showFilterModal = true;
  }

  closeFilterModal(): void {
    this.showFilterModal = false;
  }

  clearFilter(): void {
    this.filterCriteria = { // Reset to defaults
        from: '', to: '', subject: '', hasWords: '', doesntHave: '', dateWithin: '1w', hasAttachment: ''
    };
    // Close modal and reload immediately if desired, or wait for explicit Apply
    this.applyFilter(); 
  }

  applyFilter(): void {
    // TODO: Send this.filterCriteria object along with the loadEmailsForFolder API call
    console.log('Applying filter:', this.filterCriteria);
    this.closeFilterModal();
    this.currentPage = 1;
    this.loadEmailsForFolder(this.currentFolderName.toLowerCase());
  }
  
  // --- Pagination Actions ---

  changePage(direction: number): void {
    const newPage = this.currentPage + direction;
    if (newPage > 0 && newPage <= this.totalPages) {
      this.currentPage = newPage;
      this.loadEmailsForFolder(this.currentFolderName.toLowerCase());
    }
  }

  // --- Utility ---

  private capitalize(s: string): string {
    return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();
  }
}