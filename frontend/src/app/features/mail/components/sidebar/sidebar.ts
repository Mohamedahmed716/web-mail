import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ComposeService } from '../../../../services/compose.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule,],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class Sidebar {
  constructor(private composeService: ComposeService) {}

  onComposeClick() {
    this.composeService.openCompose(); // Opens empty
  }

  menuItems = [
    { label: 'Inbox', icon: 'fa-inbox', route: '/mail/inbox' },
    { label: 'Priority Inbox', icon: 'fa-star', route: '/mail/priority-inbox'},
    { label: 'Sent', icon: 'fa-paper-plane', route: '/mail/sent' },
    { label: 'Drafts', icon: 'fa-file', route: '/mail/drafts' },

    { label: 'Trash', icon: 'fa-trash', route: '/mail/trash'},
    { label: 'Contacts', icon: 'fa-address-book', route: '/mail/contacts'},
    { label: 'Folders', icon: 'fa-solid fa-folder', route: '/mail/folders'},
  ];

  currentUser = localStorage.getItem('auth-user') ? JSON.parse(localStorage.getItem('auth-user')!) : null;
}
