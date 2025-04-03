import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VotingDialogComponent } from './voting-dialog.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('VotingDialogComponent', () => {
  let component: VotingDialogComponent;
  let fixture: ComponentFixture<VotingDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VotingDialogComponent, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VotingDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
