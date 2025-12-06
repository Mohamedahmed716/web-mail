import { Routes } from '@angular/router';
import { SigninComponent } from './features/auth/Signin/signin.component';
import { SignupComponent } from './features/auth/Sigup/signup.component';
import {ContactsComponent} from './features/contacts/ContactsComponent.component';
import {DashboardComponent} from './features/dashboard/dashboard';

export const routes: Routes = [
  { path: '', redirectTo: 'signin', pathMatch: 'full' },
  { path: 'signin', component: SigninComponent, title: 'Sign In - Mail Server' },
  { path: 'signup', component: SignupComponent, title: 'Create Account - Mail Server' },
  {
    path: 'contacts',
    component: ContactsComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent
  },  { path: '**', redirectTo: 'signin' },
];
