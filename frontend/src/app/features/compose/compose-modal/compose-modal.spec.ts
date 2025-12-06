import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComposeModal } from './compose-modal';

describe('ComposeModal', () => {
  let component: ComposeModal;
  let fixture: ComponentFixture<ComposeModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComposeModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComposeModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
