import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComposeService } from '../../../../services/compose.service';

@Component({
  selector: 'app-compose',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compose.html',
  styleUrls: ['./compose.css']
})
export class Compose implements OnInit {
  isVisible = false;

  // these will be replaced with a proper model later
  attachments: File[] = [];
  email: any = {
    to: '',
    subject: '',
    body: '',
    priority: 'medium'
  };

  constructor(private composeService: ComposeService) {}

  ngOnInit() {
    this.composeService.isOpen$.subscribe(open => {
      this.isVisible = open;
    });

    this.composeService.currentDraft$.subscribe(draft => {
      if (draft) {
        this.email = { ...draft };

      } else {
        this.email = { to: '', subject: '', body: '', priority: 'medium' };
        this.attachments = [];
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
        this.attachments.push(files[i]);
      }
    }
  }

  removeAttachment(index: number) {
    this.attachments.splice(index, 1);
  }

  sendEmail() {
    console.log('Sending:', this.email, 'Attachments:', this.attachments);
    // api call would go here
    this.close();
  }

  saveDraft() {
    console.log('Saving Draft:', this.email);
    // api call would go here
    this.close();
  }
}
