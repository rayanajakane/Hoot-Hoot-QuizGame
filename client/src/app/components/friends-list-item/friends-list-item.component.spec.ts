import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsListItemComponent } from './friends-list-item.component';

describe('FriendsListItemComponent', () => {
  let component: FriendsListItemComponent;
  let fixture: ComponentFixture<FriendsListItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendsListItemComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(FriendsListItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
