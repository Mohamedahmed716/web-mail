/* tslint:disable:no-unused-variable */
import {  ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { EmailDisplayComponent } from './EmailDisplay.component';

describe('EmailDisplayComponent', () => {
  let component: EmailDisplayComponent;
  let fixture: ComponentFixture<EmailDisplayComponent>;

 

  beforeEach(() => {
    fixture = TestBed.createComponent(EmailDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
