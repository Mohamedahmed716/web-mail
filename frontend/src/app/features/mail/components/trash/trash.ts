import { Component, OnInit } from '@angular/core';
import { TrashService } from '../../../../services/trash.service';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';
import { EmailDisplayComponent } from '../EmailDisplay/EmailDisplay.component';

@Component({
  selector: 'app-trash',
  standalone: true,
  imports: [CommonModule, EmailDisplayComponent],
  templateUrl: './trash.html',
  styleUrls: ['./trash.css']
})
export class Trash implements OnInit {

  emails: Email[] = [];
  selected: Email | null = null;
  
  // NEW: Selection state for bulk operations
  selectedEmailIds: string[] = []; 

  constructor(private trash: TrashService) {}

  ngOnInit() {
    this.loadTrash();
  }

  loadTrash() {
    // Note: The subscribe callback needs to assign to this.emails
    this.trash.getTrash().subscribe(res => this.emails = res);
    this.selectedEmailIds = []; // Clear selection on load
  }

  // NEW: Toggle single email selection
  toggleSelectEmail(emailId: string, event: Event): void {
    event.stopPropagation();
    if (this.selectedEmailIds.includes(emailId)) {
      this.selectedEmailIds = this.selectedEmailIds.filter(id => id !== emailId);
    } else {
      this.selectedEmailIds.push(emailId);
    }
  }

  // NEW: Toggle select all
  toggleSelectAll(event: any): void {
    if (event.target.checked) {
      this.selectedEmailIds = this.emails.map(e => e.id);
    } else {
      this.selectedEmailIds = [];
    }
  }

  // --- Single Operations (Keep for now, but focus on bulk below) ---
  restore(email: Email) {
    this.trash.restore(email.id).subscribe(() => this.loadTrash());
  }

  deleteForever(email: Email) {
    this.trash.deleteForever(email.id).subscribe(() => this.loadTrash());
  }

  // --- NEW: Bulk Operations ---

  bulkRestoreSelected(): void {
    if (this.selectedEmailIds.length === 0) return;
    
    this.selectedEmailIds.forEach(id => {
    this.trash.restore(id).subscribe(() => {
      this.loadTrash();
    });
  });
  }

  bulkDeleteForeverSelected(): void {
    if (this.selectedEmailIds.length === 0) return;
    
    this.trash.bulkDeleteForever(this.selectedEmailIds).subscribe({
      next: () => {
        this.selectedEmailIds = [];
        this.loadTrash(); // Reload after success
      },
      error: (err) => {
        console.error('Bulk permanent delete failed:', err);
      }
    });
  }
  
}