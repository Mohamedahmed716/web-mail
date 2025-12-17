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
  styleUrls: ['./folders.css'],
})
export class FolderListComponent implements OnInit {
  private SYSTEM_FOLDERS = ['Inbox', 'Sent', 'Drafts', 'Trash', 'Priority Inbox', 'Contacts'];
  folders: string[] = [];

  // Existing Modal State
  showModal: boolean = false;
  editingFolder: string | null = null;
  newFolderName: string = '';
  errorMessage: string = '';

  // New Delete Modal State
  showDeleteModal: boolean = false;
  folderToDelete: string | null = null;

  constructor(private folderService: FolderService) {}

  ngOnInit(): void {
    this.loadFolders();
  }

  loadFolders(): void {
    this.folderService.getAllFolders().subscribe({
      next: (data) => {
        const systemFoldersLower = this.SYSTEM_FOLDERS.map((f) => f.toLowerCase());
        this.folders = data.filter(
          (folderName) => !systemFoldersLower.includes(folderName.toLowerCase())
        );
      },
      error: (err) => {
        console.error('Failed to load folders:', err);
        this.errorMessage = 'Could not load folder list.';
      },
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
      this.folderService.renameFolder(this.editingFolder, this.newFolderName).subscribe({
        next: () => {
          this.loadFolders();
          this.showModal = false;
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Failed to rename folder.';
        },
      });
    } else {
      this.folderService.createFolder(this.newFolderName).subscribe({
        next: () => {
          this.loadFolders();
          this.showModal = false;
        },
        error: (err) => {
          this.errorMessage =
            err.error.message || 'Failed to create folder. Name may already exist.';
        },
      });
    }
  }

  // Updated Delete Logic
  deleteFolder(folderName: string): void {
    this.folderToDelete = folderName;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (this.folderToDelete) {
      this.folderService.deleteFolder(this.folderToDelete).subscribe({
        next: () => {
          this.loadFolders();
          this.closeDeleteModal();
        },
        error: (err) => {
          this.errorMessage = err.error.message || 'Failed to delete folder.';
          this.closeDeleteModal();
        },
      });
    }
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.folderToDelete = null;
  }

  isSystemFolder(folderName: string): boolean {
    return ['Inbox', 'Sent', 'Trash', 'Drafts'].includes(folderName);
  }
}
