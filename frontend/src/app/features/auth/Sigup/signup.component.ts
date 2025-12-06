import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent implements OnInit {
  signupForm: FormGroup;
  errorMessage: string = '';
  isSubmitted: boolean = false;
  isLoading: boolean = false;
  showPassword: boolean = false;

  // Password Strength State
  strengthLabel: string = '';
  strengthClass: string = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.signupForm = this.fb.group(
      {
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        email: ['', Validators.required],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
        terms: [false, Validators.requiredTrue],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  ngOnInit(): void {
    this.signupForm.get('password')?.valueChanges.subscribe((val) => {
      this.updateStrength(val);
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  updateStrength(password: string): void {
    if (!password) {
      this.strengthLabel = '';
      this.strengthClass = '';
      return;
    }
    if (password.length < 6) {
      this.strengthLabel = 'Weak';
      this.strengthClass = 'weak';
    } else if (password.length < 10) {
      this.strengthLabel = 'Medium';
      this.strengthClass = 'medium';
    } else {
      this.strengthLabel = 'Strong';
      this.strengthClass = 'strong';
    }
  }

  hasError(controlName: string): boolean {
    const control = this.signupForm.get(controlName);
    return !!(control?.invalid && (control?.touched || this.isSubmitted));
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.errorMessage = '';

    if (this.signupForm.invalid) {
      return;
    }

    this.isLoading = true;

    const val = this.signupForm.value;
    const userPayload = {
      name: `${val.firstName} ${val.lastName}`,
      email: val.email,
      password: val.password,
    };

    this.authService.register(userPayload).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/signin'], { queryParams: { registered: 'true' } });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage = 'Unable to connect to server. Is the backend running?';
        } else {
          this.errorMessage = err.error || 'Sign up failed. Please try again.';
        }
      },
    });
  }
}
