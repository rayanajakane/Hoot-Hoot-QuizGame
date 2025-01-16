import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef, TextOnlySnackBar } from '@angular/material/snack-bar';
import { NOTFICATION_DURATION } from '@app/constants/feedback-messages';

@Injectable({
    providedIn: 'root',
})
export class NotificationService {
    constructor(
        public snackBar: MatSnackBar,
        public dialog: MatDialog,
    ) {}

    displayErrorMessage(errorMessage: string): MatSnackBarRef<TextOnlySnackBar> {
        return this.openSnackBar(errorMessage, '✖', {
            duration: NOTFICATION_DURATION,
            panelClass: ['error-snackbar'],
        });
    }

    displaySuccessMessage(successMessage: string): MatSnackBarRef<TextOnlySnackBar> {
        return this.openSnackBar(successMessage, '✔', {
            duration: NOTFICATION_DURATION,
            panelClass: ['success-snackbar'],
        });
    }

    displayErrorMessageAction(errorMessage: string, action: string): MatSnackBarRef<TextOnlySnackBar> {
        return this.openSnackBar(errorMessage, action);
    }

    private openSnackBar(message: string, action: string, options?: MatSnackBarConfig): MatSnackBarRef<TextOnlySnackBar> {
        return this.snackBar.open(message, action, options);
    }
}
