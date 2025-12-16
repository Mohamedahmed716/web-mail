import { Routes } from '@angular/router';
import { SigninComponent } from './features/auth/Signin/signin.component';
import { SignupComponent } from './features/auth/Sigup/signup.component';
import { ForgotPasswordComponent } from './features/auth/ForgotPassword/forgot-password.component';

export const routes: Routes = [
  { path: '', redirectTo: 'signin', pathMatch: 'full' },
  { path: 'signin', component: SigninComponent, title: 'Sign In - Mail Server' },
  { path: 'signup', component: SignupComponent, title: 'Create Account - Mail Server' },
  { path: 'forgot-password', component: ForgotPasswordComponent, title: 'Reset Password - Mail Server' },
  {
    path: 'mail',
    loadChildren: () => import('./features/mail/mail.routes').then(m => m.MAIL_ROUTES),
    title: 'My Mail'
  },
  { path: '**', redirectTo: 'signin' },
];
