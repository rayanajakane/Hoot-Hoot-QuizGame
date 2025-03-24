import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartyConfigDialogComponent } from './party-config-dialog.component';

describe('PartyConfigDialogComponent', () => {
  let component: PartyConfigDialogComponent;
  let fixture: ComponentFixture<PartyConfigDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PartyConfigDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PartyConfigDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
