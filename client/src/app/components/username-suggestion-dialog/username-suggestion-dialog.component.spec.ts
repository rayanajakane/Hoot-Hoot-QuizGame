import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsernameSuggestionDialogComponent } from './username-suggestion-dialog.component';

describe('UsernameSuggestionDialogComponent', () => {
    let component: UsernameSuggestionDialogComponent;
    let fixture: ComponentFixture<UsernameSuggestionDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [UsernameSuggestionDialogComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(UsernameSuggestionDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
