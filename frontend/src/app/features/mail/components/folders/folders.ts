import { Component, OnInit } from '@angular/core';
import { FolderService } from '../../../../services/folder';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-folder-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './folders.html',
  styleUrls: ['./folders.css']
})
export class FolderListComponent implements OnInit {
  private SYSTEM_FOLDERS = [
      'Inbox', 'Sent', 'Drafts', 'Trash', 'Priority Inbox', 'Contacts'
  ];
  folders: string[] = []; 
  showModal: boolean = false;
  editingFolder: string | null = null; 
  newFolderName: string = '';
  errorMessage: string = '';

  constructor(private folderService: FolderService) {}

  ngOnInit(): void {
    this.loadFolders();
  }

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

  // --- CRUD Modal Handlers ---
  openCreateModal(): void {
    this.editingFolder = null;
    this.newFolderName = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  openRenameModal(folderName: string): void {
    this.editingFolder = folderName;
    this.newFolderName = folderName;
    this.errorMessage = '';
    this.showModal = true;
  }

  saveFolder(): void {
    this.errorMessage = '';
    if (!this.newFolderName.trim()) {
      this.errorMessage = 'Folder name cannot be empty.';
      return;
    }

    if (this.editingFolder) {
      // RENAME OPERATION
      this.folderService.renameFolder(this.editingFolder, this.newFolderName).subscribe({
        next: () => {
          this.loadFolders();
          this.showModal = false;
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Failed to rename folder.';
        }
      });
    } else {
      // CREATE OPERATION
      this.folderService.createFolder(this.newFolderName).subscribe({
        next: () => {
          this.loadFolders();
          this.showModal = false;
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Failed to create folder. Name may already exist.';
        }
      });
    }
  }

  deleteFolder(folderName: string): void {
    if (confirm(`Are you sure you want to delete the folder "${folderName}"? All contained emails will be moved to the Inbox.`)) {
      this.folderService.deleteFolder(folderName).subscribe({
        next: () => { this.loadFolders(); },
        error: (err) => { alert(err.error.message || 'Failed to delete folder.'); }
      });
    }
  }

  isSystemFolder(folderName: string): boolean {
    return ['Inbox', 'Sent', 'Trash', 'Drafts'].includes(folderName);
  }
}