import { Component, Input, OnInit } from '@angular/core';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';
import { ComposeService } from '../../../../services/compose.service';

@Component({
  selector: 'app-EmailDisplay',
  templateUrl: './EmailDisplay.component.html',
  styleUrls: ['./EmailDisplay.component.css'],
  standalone: true,
  imports: [CommonModule],
})
export class EmailDisplayComponent implements OnInit {
  @Input() mail: Email | null = null;
  @Input() type: string = '';

  private readonly API_URL = 'http://localhost:8080/api/attachments/download';

  constructor(private composeService: ComposeService) {}

  ngOnInit() {}


  getFileType(fileName: any): 'image' | 'link' | 'pdf' | 'file' {
    if (!fileName) return 'file';

    if (typeof fileName === 'string' && (fileName.startsWith('http') || fileName.startsWith('https'))) {
      return 'link';
    }

    const lowerName = fileName.toLowerCase();

    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
    if (imageExtensions.some((ext: string) => lowerName.endsWith(ext))) {
      return 'image';
    }

    if (lowerName.endsWith('.pdf')) {
      return 'pdf';
    }

    return 'file';
  }

  getFileName(fileName: any): string {
    if (typeof fileName === 'string' && fileName.startsWith('http')) return 'External Link';

    if (typeof fileName === 'string') {
      return fileName.replace(/^.*[\\\/]/, '');
    }
    return 'Unknown File';
  }
// زودنا parameter اختياري اسمه downloadMode
  getAttachmentUrl(fileName: any, downloadMode: boolean = false): string {
    
    if (typeof fileName === 'string' && fileName.startsWith('http')) return fileName;

    const sender = this.mail?.sender;
    const senderEmail = sender && typeof sender === 'object' && 'email' in sender ? (sender as any).email : sender;

    let url = `${this.API_URL}?file=${fileName}&email=${senderEmail}`;

    // لو عايز تحميل، زود المود في الرابط
    if (downloadMode) {
      url += '&mode=download';
    }

    return url;
  }

  openReply() {
    const receivers = this.mail?.sender ? [this.mail.sender] : [];
    let draft: Email = {
      id: '',
      sender: '',
      receivers: receivers,
      subject: this.mail?.subject ? `Re: ${this.mail.subject}` : '',
      body: '',
      priority: 3,
      folder: 'Drafts',
      timestamp: new Date(),
      attachments: [],    
      attachmentNames: [] 
    };
    this.composeService.openCompose(draft);
  }

  openDraft() {
    this.composeService.openCompose(this.mail);
  }
}