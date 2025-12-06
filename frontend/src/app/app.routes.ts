import { Routes } from '@angular/router';
import { SigninComponent } from './features/auth/Signin/signin.component';
import { SignupComponent } from './features/auth/Sigup/signup.component';

export const routes: Routes = [
  { path: '', redirectTo: 'signin', pathMatch: 'full' },
  { path: 'signin', component: SigninComponent, title: 'Sign In - Mail Server' },
  { path: 'signup', component: SignupComponent, title: 'Create Account - Mail Server' },
  { path: '**', redirectTo: 'signin' },
];
