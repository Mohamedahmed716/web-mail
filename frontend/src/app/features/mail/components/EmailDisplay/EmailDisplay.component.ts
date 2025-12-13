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

  constructor(private composeService: ComposeService) { }

  ngOnInit() {}
  getFileType(fileName: any): 'image' | 'link' | 'file' {

    if (!fileName) return 'file';

    if (fileName.startsWith('http') || fileName.startsWith('https')) {
      return 'link';
    }

    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
    const lowerName = fileName.toLowerCase();

    if (imageExtensions.some((ext: string) => lowerName.endsWith(ext))) {
      return 'image';
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

  getAttachmentUrl(fileName: any): string {


    if (typeof fileName === 'string' && fileName.startsWith('http')) return fileName;

    const sender = this.mail?.sender;
    const senderEmail =
      sender && typeof sender === 'object' && 'email' in sender ? (sender as any).email : sender;

    return `http://localhost:8080/api/attachments/download?file=${fileName}&email=${senderEmail}`;
  }

  openReply(){
    const recievers = this.mail?.sender ? [this.mail.sender] : [];
    let draft: Email = {
      id: '',
      sender: '',
      receivers: recievers,
      subject: this.mail?.subject ? `Re: ${this.mail.subject}` : '',
      body: '',
      priority: 3,
      folder: 'Drafts',
      timestamp: new Date(),
      attachments: [],    // Start empty
      attachmentNames: [] // Start empty
    };
    this.composeService.openCompose(draft);
  }

  openDraft(){
    this.composeService.openCompose(this.mail);
  }

}
