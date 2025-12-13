import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Contact, ContactService } from '../../../../services/contact-service';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ContactsComponent.component.html',
  styleUrls: ['./ContactsComponent.component.css']
})
export class ContactsComponent implements OnInit {

  contacts: Contact[] = [];
  filteredContacts: Contact[] = [];
  searchQuery: string = '';
  searchType: string = 'default'; // default, name, email
  sortBy = 'name';

  showAddModal = false;
  editingContact: Contact | null = null;
  showConfirmDialog = false;
  contactToDelete: Contact | null = null;
  showSuccessToast = false;

  formName = '';
  formEmails: string[] = [''];

  loading = false;
  error = '';

  constructor(
    private contactService: ContactService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadContacts();
  }

  loadContacts() {
    this.loading = true;
    this.contactService.getAllContacts().subscribe({
      next: (data) => {
        this.contacts = data;
        this.filteredContacts = data;
        this.sortContacts();
        this.loading = false;
      },
      error: () => {
        this.error = "Failed to load contacts";
        this.loading = false;
      }
    });
  }

  onSearch() {
    if (!this.searchQuery.trim()) {
      this.filteredContacts = [...this.contacts];
      return;
    }

    this.contactService.searchContacts(this.searchQuery, this.searchType).subscribe({
      next: (data) => this.filteredContacts = data,
      error: () => this.error = "Search failed"
    });
  }

  onSearchTypeChange() {
    if (this.searchQuery.trim()) {
      this.onSearch();
    }
  }

  sortContacts() {
    this.filteredContacts = [...this.filteredContacts].sort((a, b) => {
      if (this.sortBy === 'name') return a.name.localeCompare(b.name);
      const emailA = a.emails[0] || '';
      const emailB = b.emails[0] || '';
      return emailA.localeCompare(emailB);
    });
  }

  onSortChange() {
    this.sortContacts();
  }

  openAddModal() {
    this.editingContact = null;
    this.resetForm();
    this.showAddModal = true;
  }

  openEditModal(contact: Contact) {
    this.editingContact = contact;
    this.formName = contact.name;
    this.formEmails = [...contact.emails];
    this.showAddModal = true;
  }

  closeModal() {
    this.showAddModal = false;
    this.editingContact = null;
    this.resetForm();
  }

  resetForm() {
    this.formName = '';
    this.formEmails = [''];
  }

  addEmailField() {
    this.formEmails.push('');
  }

  removeEmailField(i: number) {
    if (this.formEmails.length > 1) {
      this.formEmails.splice(i, 1);
    }
  }

  saveContact() {
    const filteredEmails = this.formEmails.filter(e => e.trim() !== '');

    if (!this.formName.trim() || filteredEmails.length === 0) {
      this.error = 'Name and at least one email are required';
      return;
    }

    const contact: Contact = {
      name: this.formName.trim(),
      emails: filteredEmails
    };

    if (this.editingContact) {
      this.contactService.updateContact(this.editingContact.id!, contact).subscribe({
        next: () => {
          this.loadContacts();
          this.closeModal();
          this.showSuccessMessage('Contact updated successfully!');
        },
        error: () => this.error = "Failed to update contact"
      });
    } else {
      this.contactService.addContact(contact).subscribe({
        next: () => {
          this.loadContacts();
          this.closeModal();
          this.showSuccessMessage('Contact added successfully!');
        },
        error: () => this.error = "Failed to add contact"
      });
    }
  }

  deleteContact(contact: Contact) {
    this.contactToDelete = contact;
    this.showConfirmDialog = true;
  }

  confirmDelete() {
    if (!this.contactToDelete) return;

    this.contactService.deleteContact(this.contactToDelete.id!).subscribe({
      next: () => {
        this.loadContacts();
        this.showConfirmDialog = false;
        this.contactToDelete = null;
        this.showSuccessMessage('Contact deleted successfully!');
      },
      error: () => {
        this.error = "Failed to delete contact";
        this.showConfirmDialog = false;
        this.contactToDelete = null;
      }
    });
  }

  cancelDelete() {
    this.showConfirmDialog = false;
    this.contactToDelete = null;
  }

  successMessage = '';

  showSuccessMessage(message: string = 'Contact deleted successfully!') {
    this.successMessage = message;
    this.showSuccessToast = true;
    setTimeout(() => {
      this.showSuccessToast = false;
    }, 3000);
  }

  goToInbox() {
    this.router.navigate(['/mail/inbox']);
  }

  trackByContactId(index: number, contact: Contact) {
    return contact.id || index;
  }

  trackByIndex(index: number) {
    return index;
  }
}
