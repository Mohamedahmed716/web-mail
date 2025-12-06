import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Contact, ContactService } from '../../services/contact-service';

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
  sortBy = 'name';

  showAddModal = false;
  editingContact: Contact | null = null;

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

    this.contactService.searchContacts(this.searchQuery).subscribe({
      next: (data) => this.filteredContacts = data,
      error: () => this.error = "Search failed"
    });
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
        },
        error: () => this.error = "Failed to update contact"
      });
    } else {
      this.contactService.addContact(contact).subscribe({
        next: () => {
          this.loadContacts();
          this.closeModal();
        },
        error: () => this.error = "Failed to add contact"
      });
    }
  }

  deleteContact(contact: Contact) {
    if (!confirm(`Delete ${contact.name}?`)) return;

    this.contactService.deleteContact(contact.id!).subscribe({
      next: () => this.loadContacts(),
      error: () => this.error = "Failed to delete contact"
    });
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  trackByContactId(index: number, contact: Contact) {
    return contact.id || index;
  }

  trackByIndex(index: number) {
    return index;
  }
}
