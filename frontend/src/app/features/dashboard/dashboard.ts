// src/app/pages/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { StorageService } from '../../core/services/storage.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  user: any = null;
  token: string | null = null;

  constructor(
    private storageService: StorageService,
    private router: Router
  ) {}

  ngOnInit() {
    // Get user and token
    this.user = this.storageService.getUser();
    this.token = this.storageService.getToken();

    console.log('Dashboard - User:', this.user);
    console.log('Dashboard - Token:', this.token);

    // If no token, redirect to login
    if (!this.token) {
      console.error('No token found! Redirecting to login...');
      this.router.navigate(['/signin']);
    }
  }

  logout() {
    this.storageService.clean();
    this.router.navigate(['/signin']);
  }

  goToContacts() {
    this.router.navigate(['/contacts']);
  }
}
