import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpHeaders } from '@angular/common/http';
import { ComposeService } from '../../../../services/compose.service';
import { ApiService } from '../../../../core/services/api.service';
import { Email } from '../../../../shared/models/email';

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

  constructor(private composeService: ComposeService, private apiService: ApiService) {}

  ngOnInit() {
    this.composeService.isOpen$.subscribe(open => {
      this.isVisible = open;
    });

    this.composeService.currentDraft$.subscribe(draft => {
      if (draft) {
        this.email = draft;
        this.receiverEmails = draft.receivers ? draft.receivers.join(', ') : '';
      } else {
        this.email = {
          id: '',
          sender: '',
          attachments: [],
          receivers: [],
          subject: '',
          body: '',
          timestamp: new Date(),
          priority: 3,
          folder: 'Drafts'
        };
        this.receiverEmails = '';
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
        if (!this.email!.attachments) this.email!.attachments = [];
        this.email?.attachments?.push(files[i]);
      }
    }
  }

  removeAttachment(index: number) {
    this.email?.attachments?.splice(index, 1);
  }

  private prepareFormData(email: Email): FormData {
    const formData = new FormData();

    formData.append('receivers', JSON.stringify(email.receivers));
    formData.append('id', email.id || '');
    formData.append('subject', email.subject || '');
    formData.append('body', email.body || '');
    formData.append('priority', (email.priority || 3).toString());
    formData.append('folder', email.folder || 'Drafts');

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
    if(this.email.receivers.length === 0) {
      alert('Please add at least one receiver email address.');
      return;
    }

    const token = localStorage.getItem('auth-token') || '';
    console.log('Sending Token:', token);
    const headers = new HttpHeaders().set('Authorization', token);

    const payload = this.prepareFormData(this.email);

    this.apiService.post('/send/sendEmail', payload, { headers }).subscribe({
      next: (response) => {
        console.log('Email sent successfully:', response);
        this.composeService.notifyRefresh();
        this.close();
      },
      error: (error) => {
        if (error.status === 200) {
          console.log('Draft saved successfully (Text response)');
          this.composeService.notifyRefresh();
          this.close();
        } else {
          console.error('Real Error saving Draft:', error);
        }
      }
    });
  }

  saveDraft() {
    console.log('Saving Draft:', this.email);
    if(!this.email) return;

    this.updateReceiversArray();

    const token = localStorage.getItem('auth-token') || '';
    console.log('Sending Token:', token);
    const headers = new HttpHeaders().set('Authorization', token);

    const payload = this.prepareFormData(this.email);

    this.apiService.post('/draft/saveDraft', payload, { headers }).subscribe({
      next: (response) => {
        console.log('Draft saved successfully:', response);
        this.composeService.notifyRefresh();
        this.close();
      },
      error: (error) => {
        if (error.status === 200) {
          console.log('Draft saved successfully (Text response)');
          this.composeService.notifyRefresh();
          this.close();
        } else {
          console.error('Real Error saving Draft:', error);
        }
      }
    });
  }
}
