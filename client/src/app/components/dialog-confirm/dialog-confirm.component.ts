import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmDialogData } from '@app/interfaces/dialog-data/confirm-dialog-data';
import { AdminEditPageComponent } from '@app/pages/admin-edit-page/admin-edit-page.component';

@Component({
    selector: 'app-dialog-confirm',
    templateUrl: './dialog-confirm.component.html',
    styleUrls: ['./dialog-confirm.component.scss'],
})
export class DialogConfirmComponent {
    constructor(
        private readonly dialogRef: MatDialogRef<AdminEditPageComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData,
    ) {
        dialogRef.disableClose = true;
    }

    onCancel(): void {
        this.dialogRef.close(false);
    }

    onConfirm(): void {
        this.dialogRef.close(true);
    }
}
