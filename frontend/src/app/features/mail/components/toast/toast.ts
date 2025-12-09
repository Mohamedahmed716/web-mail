import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../../../core/services/toast-service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toasts"
           class="toast-item"
           [ngClass]="toast.type"
           (click)="remove(toast.id!)">

        <div class="icon">
          <i class="fa-solid" [ngClass]="toast.type === 'success' ? 'fa-check-circle' : 'fa-circle-exclamation'"></i>
        </div>
        <div class="message">{{ toast.text }}</div>

      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999; /* On top of modals */
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .toast-item {
      min-width: 300px;
      padding: 16px;
      border-radius: 8px;
      background-color: #1f2937; /* Dark gray bg */
      color: #fff;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5);
      cursor: pointer;
      animation: slideIn 0.3s ease-out;
      border-left: 4px solid transparent;
    }

    /* Success Style */
    .toast-item.success {
      border-left-color: #10b981; /* Green */
    }
    .toast-item.success .icon { color: #10b981; }

    /* Error Style */
    .toast-item.error {
      border-left-color: #ef4444; /* Red */
    }
    .toast-item.error .icon { color: #ef4444; }

    .message {
      font-size: 0.9rem;
      font-weight: 500;
    }

    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class Toast implements OnInit {
  toasts: ToastMessage[] = [];

  constructor(private toastService: ToastService) {}

  ngOnInit() {
    this.toastService.toasts$.subscribe(toasts => {
      this.toasts = toasts;
    });
  }

  remove(id: number) {
    this.toastService.removeToast(id);
  }
}
