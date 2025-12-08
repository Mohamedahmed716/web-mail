import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComposeService } from '../../../../services/compose.service';
import {Email} from '../../../../shared/models/email';
import {User} from '../../../../shared/models/user';
import {ApiService} from '../../../../core/services/api.service';

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
        this.receiverEmails = this.email.receivers.map(r => r.email).join(', ');
      } else {
        this.email = {
          id: '',
          sender: {
            id: '',
            name: '',
            email: ''
          } as User,
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
        this.email?.attachments?.push(files[i]);
      }
    }
  }

  removeAttachment(index: number) {
    this.email?.attachments?.splice(index, 1);
  }

  sendEmail() {
    console.log('Sending:', this.email);
    if(!this.email) {
      return;
    }
    this.apiService.post('/sendEmail',this.email).subscribe({
      next: (response) => {
        console.log('Email sent successfully:', response);
        this.close();
      },
      error: (error) => {
        console.error('Error sending email:', error);
      }
    })
  }

  saveDraft() {
    console.log('Saving Draft:', this.email);
    if(!this.email) {
      return;
    }
    this.apiService.post('/saveDraft' ,this.email).subscribe({
      next: (response) => {
        console.log('Draft saved successfully:', response);
        this.composeService.notifyRefresh();
        this.close();
      },
      error: (error) => {
        console.error('Error saving Draft:', error);
      }
    })
  }
}
