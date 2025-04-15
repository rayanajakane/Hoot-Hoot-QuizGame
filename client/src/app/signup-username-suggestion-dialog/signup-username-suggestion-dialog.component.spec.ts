import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';
import { of } from 'rxjs';
import { SignupUsernameSuggestionDialogComponent } from './signup-username-suggestion-dialog.component';

describe('SignupUsernameSuggestionDialogComponent', () => {
    let component: SignupUsernameSuggestionDialogComponent;
    let fixture: ComponentFixture<SignupUsernameSuggestionDialogComponent>;

    beforeEach(async () => {
        const suggestionSpy = jasmine.createSpyObj('UsernameSuggestionService', ['getFrenchUsernameSuggestions']);
        await TestBed.configureTestingModule({
            imports: [MatDialogModule],
            declarations: [SignupUsernameSuggestionDialogComponent],
            providers: [
                { provide: UsernameSuggestionService, useValue: suggestionSpy },
                {
                    provide: MatDialogRef,
                    useValue: { close: () => of(true) },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SignupUsernameSuggestionDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
