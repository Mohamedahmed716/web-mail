import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

type ResetStep = 'email' | 'security' | 'password' | 'success';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './forgot-password.component.html',
    styleUrl: './forgot-password.component.css',
})
export class ForgotPasswordComponent {
    currentStep: ResetStep = 'email';

    emailForm: FormGroup;
    securityForm: FormGroup;
    passwordForm: FormGroup;

    userEmail: string = '';
    favoriteMovieAnswer: string = '';

    isLoading: boolean = false;
    errorMessage: string = '';
    successMessage: string = '';
    showPassword: boolean = false;

    readonly DOMAIN_SUFFIX = '@wegmail.com';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.emailForm = this.fb.group({
            email: ['', Validators.required],
        });

        this.securityForm = this.fb.group({
            favoriteMovie: ['', Validators.required],
        });

        this.passwordForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', Validators.required],
        }, { validators: this.passwordMatchValidator });
    }

    passwordMatchValidator(control: any) {
        const password = control.get('newPassword')?.value;
        const confirmPassword = control.get('confirmPassword')?.value;
        return password === confirmPassword ? null : { mismatch: true };
    }

    togglePassword(): void {
        this.showPassword = !this.showPassword;
    }

    // Step 1: Verify Email
    onEmailSubmit(): void {
        if (this.emailForm.invalid) return;

        this.isLoading = true;
        this.errorMessage = '';

        const emailValue = this.emailForm.get('email')?.value;
        // Append domain if not present
        this.userEmail = emailValue.includes('@')
            ? emailValue
            : emailValue + this.DOMAIN_SUFFIX;

        this.authService.verifyEmail(this.userEmail).subscribe({
            next: () => {
                this.isLoading = false;
                this.currentStep = 'security';
            },
            error: (err) => {
                this.isLoading = false;
                if (err.status === 404) {
                    this.errorMessage = 'Email not found in our system.';
                } else if (err.status === 0) {
                    this.errorMessage = 'Unable to connect to server.';
                } else {
                    this.errorMessage = err.error || 'Verification failed.';
                }
            },
        });
    }

    // Step 2: Verify Security Question
    onSecuritySubmit(): void {
        if (this.securityForm.invalid) return;

        this.isLoading = true;
        this.errorMessage = '';

        this.favoriteMovieAnswer = this.securityForm.get('favoriteMovie')?.value;

        this.authService.verifySecurityQuestion(this.userEmail, this.favoriteMovieAnswer).subscribe({
            next: (response) => {
                this.isLoading = false;
                this.successMessage = response.message || 'Identity verified. Please enter your new password.';
                this.currentStep = 'password';
            },
            error: (err) => {
                this.isLoading = false;
                if (err.status === 401) {
                    this.errorMessage = 'Incorrect answer. Access denied.';
                } else if (err.status === 0) {
                    this.errorMessage = 'Unable to connect to server.';
                } else {
                    this.errorMessage = err.error || 'Verification failed.';
                }
            },
        });
    }

    // Step 3: Reset Password
    onPasswordSubmit(): void {
        if (this.passwordForm.invalid) return;

        this.isLoading = true;
        this.errorMessage = '';

        const newPassword = this.passwordForm.get('newPassword')?.value;

        this.authService.resetPassword(this.userEmail, this.favoriteMovieAnswer, newPassword).subscribe({
            next: (response) => {
                this.isLoading = false;
                this.successMessage = response.message || 'Password reset successful!';
                this.currentStep = 'success';
            },
            error: (err) => {
                this.isLoading = false;
                if (err.status === 0) {
                    this.errorMessage = 'Unable to connect to server.';
                } else {
                    this.errorMessage = err.error || 'Password reset failed.';
                }
            },
        });
    }

    goBack(): void {
        if (this.currentStep === 'security') {
            this.currentStep = 'email';
        } else if (this.currentStep === 'password') {
            this.currentStep = 'security';
        }
        this.errorMessage = '';
        this.successMessage = '';
    }

    goToLogin(): void {
        this.router.navigate(['/signin']);
    }
}
