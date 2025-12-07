import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ComposeService } from '../../../../services/compose.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class Sidebar {
  constructor(private composeService: ComposeService) {}

  onComposeClick() {
    this.composeService.openCompose(); // Opens empty
  }

  menuItems = [
    { label: 'Inbox', icon: 'fa-inbox', route: '/mail/inbox', badge: 12 },
    { label: 'Priority Inbox', icon: 'fa-star', route: '/mail/priority', badge: 3 }, // Added priority based on requirements
    { label: 'Sent', icon: 'fa-paper-plane', route: '/mail/sent', badge: 0 },
    { label: 'Drafts', icon: 'fa-file', route: '/mail/drafts', badge: 2 },
    { label: 'Trash', icon: 'fa-trash', route: '/mail/trash', badge: 5 },
    { label: 'Contacts', icon: 'fa-envelope', route: '/mail/contacts', badge: 0 },
  ];

  currentUser = {
    name: 'John Doe',
    email: 'john@email.com',
    avatar: 'https://i.pravatar.cc/150?img=11' // Placeholder image
  };
}
