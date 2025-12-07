import { TestBed } from '@angular/core/testing';

import { Compose } from './compose.service';

describe('Compose', () => {
  let service: Compose;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Compose);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
