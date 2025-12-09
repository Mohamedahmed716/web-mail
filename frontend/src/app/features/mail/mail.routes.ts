import { Routes } from '@angular/router';
import { Mail } from './mail';
import { Inbox } from './components/inbox/inbox';
import { Drafts } from './components/drafts/drafts';
import {Sent} from './components/sent/sent';
import {Trash} from './components/trash/trash';
import {ContactsComponent} from './components/contacts/ContactsComponent.component';
import { PriorityInboxComponent } from './components/PriorityInbox/PriorityInbox.component';

export const MAIL_ROUTES: Routes = [
  {
    path: '',
    component: Mail, // This is the Parent Shell containing the Sidebar
    children: [
      { path: '', redirectTo: 'inbox', pathMatch: 'full' },
      { path: 'inbox', component: Inbox },
      { path: 'priority-inbox', component: PriorityInboxComponent },
      { path: 'drafts', component: Drafts },
      { path: 'sent', component: Sent },
      { path: 'trash', component: Trash },
      { path: 'contacts', component: ContactsComponent },
      { path: '**', redirectTo: 'inbox', pathMatch: 'full' },
    ]
  }
];
