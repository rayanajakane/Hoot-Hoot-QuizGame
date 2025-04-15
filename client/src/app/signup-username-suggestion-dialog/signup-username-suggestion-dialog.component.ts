import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';

@Component({
    selector: 'app-signup-username-suggestion-dialog',
    templateUrl: './signup-username-suggestion-dialog.component.html',
    styleUrl: './signup-username-suggestion-dialog.component.scss',
})
export class SignupUsernameSuggestionDialogComponent implements OnInit {
    selectedUsername: string;
    constructor(
        private readonly dialogRef: MatDialogRef<SignupUsernameSuggestionDialogComponent>,
        public usernameSuggestionService: UsernameSuggestionService,
    ) {}

    ngOnInit() {
        this.usernameSuggestionService.getFrenchUsernameSuggestions();
        this.selectedUsername = '';
    }

    onCancel(): void {
        this.dialogRef.close('');
    }

    onConfirm(): void {
        this.dialogRef.close(this.selectedUsername);
    }

    regenerate() {
        this.usernameSuggestionService.getFrenchUsernameSuggestions();
        this.selectedUsername = '';
    }
}
