import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuackListComponent } from './quack-list.component';

describe('QuackListComponent', () => {
  let component: QuackListComponent;
  let fixture: ComponentFixture<QuackListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuackListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuackListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
