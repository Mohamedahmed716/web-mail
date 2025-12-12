import { TestBed } from '@angular/core/testing';

import { PriorityInboxService } from './priority-inbox.service';

describe('PriorityInboxService', () => {
  let service: PriorityInboxService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PriorityInboxService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});