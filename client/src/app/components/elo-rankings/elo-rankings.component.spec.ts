import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EloRankingsComponent } from './elo-rankings.component';

describe('EloRankingsComponent', () => {
  let component: EloRankingsComponent;
  let fixture: ComponentFixture<EloRankingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EloRankingsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EloRankingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
