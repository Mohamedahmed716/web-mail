import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StorageService } from '../../../core/services/storage.service';

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signin.component.html',
  styleUrl: './signin.component.css',
})
export class SigninComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string = '';
  isSubmitted: boolean = false;
  isLoading: boolean = false;
  showPassword: boolean = false;
  showSuccessToast: boolean = false;
  toastMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private storageService: StorageService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required],
      rememberMe: [false],
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      if (params['registered'] === 'true') {
        this.toastMessage = 'Account created successfully! Please sign in.';
        this.showSuccessToast = true;
        setTimeout(() => {
          this.showSuccessToast = false;
        }, 4000);
      }
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  hasError(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    return !!(control?.invalid && (control?.touched || this.isSubmitted));
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (data) => {
        this.storageService.saveToken(data.token);
        this.storageService.saveUser(data.user);
        this.isLoading = false;
        console.log('Login Success');
        this.router.navigate(['/mail']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage = 'Unable to connect to server.';
        } else {
          this.errorMessage = err.error || 'Invalid credentials';
        }
      },
    });
  }
}
