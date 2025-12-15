import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpHeaders } from '@angular/common/http';
import { ComposeService } from '../../../../services/compose.service';
import { ApiService } from '../../../../core/services/api.service';
import { Email } from '../../../../shared/models/email';
import { ToastService } from '../../../../core/services/toast-service';

@Component({
  selector: 'app-compose',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compose.html',
  styleUrls: ['./compose.css']
})
export class Compose implements OnInit {
  isVisible = false;
  email: Email | undefined;

  receiverEmails: string = '';

  constructor(private composeService: ComposeService, private apiService: ApiService, private toastService: ToastService) {}

  ngOnInit() {
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
}
