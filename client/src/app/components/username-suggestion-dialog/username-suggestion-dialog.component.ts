import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';

@Component({
    selector: 'app-username-suggestion-dialog',
    templateUrl: './username-suggestion-dialog.component.html',
    styleUrl: './username-suggestion-dialog.component.scss',
})
export class UsernameSuggestionDialogComponent implements OnInit {
    selectedUsername: string;
    constructor(
        private readonly dialogRef: MatDialogRef<UsernameSuggestionDialogComponent>,
        public usernameSuggestionService: UsernameSuggestionService,
    ) {}

    ngOnInit() {
        this.usernameSuggestionService.getUsernameSuggestions();
        this.selectedUsername = '';
    }

    onCancel(): void {
        this.dialogRef.close('');
    }

    onConfirm(): void {
        this.dialogRef.close(this.selectedUsername);
    }

    regenerate() {
        this.usernameSuggestionService.getUsernameSuggestions();
        this.selectedUsername = '';
    }
}
