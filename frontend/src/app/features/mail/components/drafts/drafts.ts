import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComposeService } from '../../../../services/compose.service';

@Component({
  selector: 'app-drafts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drafts.html',
  styleUrls: ['./drafts.css']
})
export class Drafts {

  drafts: (any & { id: number, date: string, hasAttachment: boolean })[] = [
    {
      id: 1,
      to: 'sarah@techcorp.com',
      subject: 'Q4 Project Timeline Update',
      body: 'Hi Sarah, I wanted to share the updated timeline...',
      priority: 'high',
      date: '10:30 AM',
      hasAttachment: true
    },
    {
      id: 2,
      to: 'marcus@design.com',
      subject: 'Design Review Meeting',
      body: 'The new mockups are ready for review. Can we schedule...',
      priority: 'medium',
      date: 'Yesterday',
      hasAttachment: false
    },
    {
      id: 3,
      to: 'hr@company.com',
      subject: 'Holiday Request',
      body: 'I would like to request time off for...',
      priority: 'low',
      date: 'Dec 3',
      hasAttachment: false
    },
  ];

  constructor(private composeService: ComposeService) {}

  openDraft(draft: any) {
    this.composeService.openCompose({
      to: draft.to,
      subject: draft.subject,
      body: draft.body,
      priority: draft.priority
    });
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }
}
