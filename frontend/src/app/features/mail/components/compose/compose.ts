import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpHeaders } from '@angular/common/http';
import { ComposeService } from '../../../../services/compose.service';
import { ApiService } from '../../../../core/services/api.service';
import { Email } from '../../../../shared/models/email';
import { ToastService } from '../../../../core/services/toast-service';
import { ContactService, Contact } from '../../../../services/contact-service';

@Component({
  selector: 'app-compose',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compose.html',
  styleUrls: ['./compose.css']
})
export class Compose implements OnInit, OnDestroy {
  isVisible = false;
  email: Email | undefined;

  receiverEmails: string = '';

  // Autocomplete properties
  showContactDropdown = false;
  filteredContacts: Contact[] = [];
  allContacts: Contact[] = [];
  currentInputValue = '';
  searchTimeout: any;
  selectedContactIndex = -1;

  constructor(
    private composeService: ComposeService,
    private apiService: ApiService,
    private toastService: ToastService,
    private contactService: ContactService
  ) {}

  ngOnInit() {
    // Load contacts for autocomplete
    this.loadContacts();

    // 1. Handle Visibility (Open/Close Modal)
    this.composeService.isOpen$.subscribe(open => {
      this.isVisible = open;
    });

    // 2. Handle Data (New Email vs. Edit Draft)
    this.composeService.currentDraft$.subscribe(draft => {
      if (draft) {
        // --- MODE: EDIT DRAFT ---
        this.email = draft;

        // A. Sync Receivers: Convert Array ["a@b.com", "c@d.com"] -> String "a@b.com, c@d.com"
        this.receiverEmails = draft.receivers ? draft.receivers.join(', ') : '';

        // B. Sync Attachments: Initialize empty array for Files
        this.email.attachments = [];

        // C. Download Logic: Convert backend "Names" -> Frontend "Files"
        if (draft.attachmentNames && draft.attachmentNames.length > 0) {

          draft.attachmentNames.forEach(fileName => {
            // Download the specific file using the Sender (Owner) email
            this.apiService.downloadFileObject(fileName, draft.sender).subscribe({
              next: (realFile) => {
                // Push the downloaded binary file into our main array
                // The UI will see this exactly like a new user upload
                this.email?.attachments?.push(realFile);
              },
              error: (err) => {
                console.error(`Failed to download attachment: ${fileName}`, err);
                // Optional: You could show a toast here like "Failed to load attachment X"
              }
            });
          });
        }

      } else {
        // --- MODE: NEW EMAIL ---
        this.email = {
          id: '',
          sender: '', // Backend extracts this from Token
          receivers: [],
          subject: '',
          body: '',
          priority: 3,
          folder: '',
          timestamp: new Date(),
          attachments: [],    // Start empty
          attachmentNames: [] // Start empty
        };

        this.receiverEmails = ''; // Reset input field
      }
    });
  }

  close() {
    this.composeService.closeCompose();
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        this.email?.attachments?.push(files[i]);
      }
    }
  }

  // Remove (Removes from the SAME array)
  removeAttachment(index: number) {
    this.email?.attachments?.splice(index, 1);
  }

  // Simplified prepareFormData
  // Since everything is now a File, we just send "attachments".
  // We rely on the Backend to handle optimization (deduplication) if it wants to.
  private prepareFormData(email: Email): FormData {
    const formData = new FormData();

    formData.append('receivers', JSON.stringify(email.receivers));
    formData.append('id', email.id || '');
    formData.append('subject', email.subject || '');
    formData.append('body', email.body || '');
    formData.append('priority', (email.priority || 3).toString());
    formData.append('folder', email.folder || 'Drafts');

    // Send ALL files (Old downloaded ones + New ones)
    // The Frontend doesn't care anymore. It's all just files.
    if (email.attachments && email.attachments.length > 0) {
      for (const file of email.attachments) {
        formData.append('attachments', file);
      }
    }

    return formData;
  }

  private updateReceiversArray() {
    if (!this.email) return;
    this.email.receivers = this.receiverEmails
      .split(',')
      .map(e => e.trim())
      .filter(e => e !== '');
  }

  sendEmail() {
    console.log('Sending:', this.email);

    if(!this.email) return;

    this.updateReceiversArray();

    // 2. Validate
    if (this.email.receivers.length === 0) {
      this.toastService.showError('Please add at least one receiver.');
      return;
    }

    const token = localStorage.getItem('auth-token') || '';
    console.log('Sending Token:', token);
    const headers = new HttpHeaders().set('Authorization', token);

    this.email.folder = 'Sent'; // Set folder to Sent on sending
    const payload = this.prepareFormData(this.email);

    this.apiService.post('/send/sendEmail', payload, { headers, responseType: 'text'}).subscribe({
      next: (response) => {
        // 2. Success Feedback
        this.toastService.showSuccess('Email sent successfully!');
        this.composeService.notifyRefresh();
        this.close();
      },
      error: (error) => {
        console.error(error);
        // 3. Backend Error Feedback
        // Extract the message from the backend response
        const errorMsg = error.error || 'Failed to send email. Please try again.';
        this.toastService.showError(errorMsg);
      }
    });
  }

  saveDraft() {
    this.updateReceiversArray();

    if(!this.email) return;

    const token = localStorage.getItem('auth-token') || '';
    const headers = new HttpHeaders().set('Authorization', token);

    this.email.folder = 'Drafts'; // Ensure folder is Drafts
    const payload = this.prepareFormData(this.email);

    this.apiService.post('/draft/saveDraft', payload, { headers, responseType: 'text' }).subscribe({
      next: (response) => {
        // 2. Success Feedback
        this.toastService.showSuccess('Draft saved successfully!');
        this.composeService.notifyRefresh();
        this.close();
      },
      error: (error) => {
        console.error(error);
        // 3. Backend Error Feedback
        // Extract the message from the backend response
        const errorMsg = error.error || 'Failed to save draft. Please try again.';
        this.toastService.showError(errorMsg);
      }
    });
  }

  openFile(file: File, event: Event) {
    // 1. Stop the click from bubbling up (optional, good practice)
    event.stopPropagation();
    event.preventDefault();

    // 2. Create a temporary URL for the file in memory
    const fileURL = URL.createObjectURL(file);

    // 3. Check if it's viewable (Image or PDF)
    if (file.type.startsWith('image/') || file.type === 'application/pdf') {
      // Open in new tab
      window.open(fileURL, '_blank');
    } else {
      // Force Download for other types
      const link = document.createElement('a');
      link.href = fileURL;
      link.download = file.name;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }

    // Note: We rely on garbage collection, but ideally you revokeURL after use
    // setTimeout(() => URL.revokeObjectURL(fileURL), 1000);
  }

  // Contact autocomplete methods
  loadContacts() {
    // Load a small set of contacts for initial display
    this.contactService.getAllContacts().subscribe({
      next: (contacts) => {
        this.allContacts = contacts;
      },
      error: (err) => {
        console.error('Failed to load contacts:', err);
      }
    });
  }

  onToFieldInput(event: any) {
    const value = event.target.value;
    this.receiverEmails = value;
    this.currentInputValue = value;

    // Clear previous timeout
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    // Get the current word being typed (after the last comma)
    const lastCommaIndex = value.lastIndexOf(',');
    const currentWord = value.substring(lastCommaIndex + 1).trim();

    if (currentWord.length >= 1) {
      // Debounce the search to avoid too many API calls
      this.searchTimeout = setTimeout(() => {
        this.searchContacts(currentWord);
      }, 300);
    } else {
      this.showContactDropdown = false;
    }
  }

  searchContacts(query: string) {
    // Use backend search for better performance
    this.contactService.searchContacts(query, 'default').subscribe({
      next: (contacts) => {
        this.filteredContacts = contacts; // Show all matching contacts
        this.showContactDropdown = this.filteredContacts.length > 0;
        this.selectedContactIndex = -1; // Reset selection

      },
      error: (err) => {
        console.error('Failed to search contacts:', err);
        // Fallback to local filtering if search fails
        this.filterContactsLocally(query);
      }
    });
  }

  filterContactsLocally(query: string) {
    const lowerQuery = query.toLowerCase();
    this.filteredContacts = this.allContacts.filter(contact => {
      // Search in contact name
      const nameMatch = contact.name.toLowerCase().includes(lowerQuery);
      
      // Search in contact emails - ONLY in username part (before @), ignore domain completely
      const emailMatch = contact.emails.some(email => {
        const emailLower = email.toLowerCase();
        
        // Always search only the username part (before @), ignore domain completely
        const atIndex = emailLower.indexOf('@');
        if (atIndex > 0) {
          const username = emailLower.substring(0, atIndex);
          return username.includes(lowerQuery);
        }
        
        // If no @ found, search the whole string (shouldn't happen with valid emails)
        return emailLower.includes(lowerQuery);
      });
      
      return nameMatch || emailMatch;
    }); // Show all matching contacts
    this.showContactDropdown = this.filteredContacts.length > 0;
    this.selectedContactIndex = -1; // Reset selection

  }

  selectContact(contact: Contact) {
    // Get the primary email (first email in the array)
    const selectedEmail = contact.emails[0];
    this.selectContactEmail(contact, selectedEmail);
  }

  selectContactEmail(contact: Contact, selectedEmail: string) {
    // Find the last comma position to replace the current word
    const lastCommaIndex = this.receiverEmails.lastIndexOf(',');

    if (lastCommaIndex >= 0) {
      // Replace the current word after the last comma
      this.receiverEmails = this.receiverEmails.substring(0, lastCommaIndex + 1) + ' ' + selectedEmail;
    } else {
      // No comma found, replace the entire field
      this.receiverEmails = selectedEmail;
    }

    // Add comma and space for next recipient
    this.receiverEmails += ', ';

    this.showContactDropdown = false;
  }

  hideContactDropdown() {
    // Small delay to allow click events on dropdown items
    setTimeout(() => {
      this.showContactDropdown = false;
    }, 150);
  }

  onToFieldFocus() {
    // Show dropdown if there's a current query
    const lastCommaIndex = this.receiverEmails.lastIndexOf(',');
    const currentWord = this.receiverEmails.substring(lastCommaIndex + 1).trim();

    if (currentWord.length >= 1) {
      this.searchContacts(currentWord);
    }
  }

  onKeyDown(event: KeyboardEvent) {
    if (!this.showContactDropdown || this.filteredContacts.length === 0) {
      return;
    }

    // Calculate total number of email options
    const totalOptions = this.filteredContacts.reduce((sum, contact) => sum + contact.emails.length, 0);

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedContactIndex = Math.min(this.selectedContactIndex + 1, totalOptions - 1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.selectedContactIndex = Math.max(this.selectedContactIndex - 1, -1);
        break;
      case 'Enter':
        event.preventDefault();
        if (this.selectedContactIndex >= 0) {
          this.selectContactByIndex(this.selectedContactIndex);
        }
        break;
      case 'Escape':
        this.showContactDropdown = false;
        this.selectedContactIndex = -1;
        break;
    }
  }

  selectContactByIndex(index: number) {
    let currentIndex = 0;
    for (const contact of this.filteredContacts) {
      for (const email of contact.emails) {
        if (currentIndex === index) {
          this.selectContactEmail(contact, email);
          return;
        }
        currentIndex++;
      }
    }
  }

  getGlobalIndex(contactIndex: number, emailIndex: number): number {
    let globalIndex = 0;
    for (let i = 0; i < contactIndex; i++) {
      globalIndex += this.filteredContacts[i].emails.length;
    }
    return globalIndex + emailIndex;
  }

  ngOnDestroy() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
  }
}
