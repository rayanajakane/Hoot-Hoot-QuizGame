import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstimatedAnswerAreaComponent } from './estimated-answer-area.component';

describe('EstimatedAnswerAreaComponent', () => {
  let component: EstimatedAnswerAreaComponent;
  let fixture: ComponentFixture<EstimatedAnswerAreaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstimatedAnswerAreaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EstimatedAnswerAreaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
