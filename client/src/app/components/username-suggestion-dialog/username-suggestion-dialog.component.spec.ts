import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { UsernameSuggestionDialogComponent } from '@app/components/username-suggestion-dialog/username-suggestion-dialog.component';
import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';
import { of } from 'rxjs';

describe('UsernameSuggestionDialogComponent', () => {
    let component: UsernameSuggestionDialogComponent;
    let fixture: ComponentFixture<UsernameSuggestionDialogComponent>;

    beforeEach(async () => {
        const suggestionSpy = jasmine.createSpyObj('UsernameSuggsetionService', ['getUsernameSuggestions']);
        await TestBed.configureTestingModule({
            imports: [MatDialogModule],
            declarations: [UsernameSuggestionDialogComponent],
            providers: [
                { provide: UsernameSuggestionService, useValue: suggestionSpy },
                {
                    provide: MatDialogRef,
                    useValue: { close: () => of(true) },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(UsernameSuggestionDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
