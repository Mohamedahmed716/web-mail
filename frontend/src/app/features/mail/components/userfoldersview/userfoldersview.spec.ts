import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserFoldersView } from './userfoldersview';

describe('Userfoldersview', () => {
  let component: UserFoldersView;
  let fixture: ComponentFixture<UserFoldersView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserFoldersView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserFoldersView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
