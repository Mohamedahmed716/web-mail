import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface ToastMessage {
  text: string;
  type: 'success' | 'error';
  id?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  // We use an array to allow multiple messages to stack if needed
  private toastSubject = new BehaviorSubject<ToastMessage[]>([]);
  toasts$ = this.toastSubject.asObservable();

  showSuccess(message: string) {
    this.addToast(message, 'success');
  }

  showError(message: string) {
    this.addToast(message, 'error');
  }

  private addToast(text: string, type: 'success' | 'error') {
    const currentToasts = this.toastSubject.value;
    const newToast: ToastMessage = { text, type, id: Date.now() };

    // Add new message to the list
    this.toastSubject.next([...currentToasts, newToast]);

    // Auto-remove after 3 seconds
    setTimeout(() => {
      this.removeToast(newToast.id!);
    }, 3000);
  }

  removeToast(id: number) {
    const currentToasts = this.toastSubject.value;
    this.toastSubject.next(currentToasts.filter(t => t.id !== id));
  }
}
