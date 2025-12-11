import { TestBed } from '@angular/core/testing';

import { AllMailService } from './all-mail.service';

describe('AllMailService', () => {
  let service: AllMailService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AllMailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});