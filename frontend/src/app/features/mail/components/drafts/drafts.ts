import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComposeService } from '../../../../services/compose.service';
import { Email } from '../../../../shared/models/email';
import { User } from '../../../../shared/models/user';
import { ApiService } from '../../../../core/services/api.service';
import {HttpHeaders, HttpParams} from '@angular/common/http';

@Component({
  selector: 'app-drafts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drafts.html',
  styleUrls: ['./drafts.css']
})
export class Drafts implements OnInit {

  drafts: Email[] = [];
  isLoading = true;
  constructor(private composeService: ComposeService, private apiService: ApiService) {}


  ngOnInit() {
    this.loadDrafts();

    this.composeService.refresh$.subscribe(() => {
      this.loadDrafts();
    });
  }

  loadDrafts() {
    this.isLoading = true;

    const token = localStorage.getItem('auth-token') || '';
    const headers = new HttpHeaders().set('Authorization', token);
    // const params : HttpParams = {
    //   page: page.toString(),
    //   size: size.toString(),
    //   sort: sort,
    // };
    this.apiService.get('/draft/loadDrafts', { headers }).subscribe({
      next: (data: Email[]) => {
        this.drafts = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading drafts:', error);
        this.isLoading = false;
      }
    })
  }

  openDraft(draft: Email) {
    this.composeService.openCompose(draft);
  }

  stopProp(event: Event) {
    event.stopPropagation();
  }

  getPriorityLabel(priority: number | undefined): string {
    if(!priority) {
      return 'medium';
    }

    switch (priority) {
      case 5: return 'Critical';
      case 4: return 'High';
      case 3: return 'Medium';
      case 2: return 'Low';
      case 1: return 'Very low';
      default: return 'medium';
    }
  }
}
