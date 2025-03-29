import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PartyConfig } from '@common/interfaces/party-config';

@Component({
    selector: 'app-party-config-dialog',
    templateUrl: './party-config-dialog.component.html',
    styleUrls: ['./party-config-dialog.component.scss'],
    standalone: false,
})
export class PartyConfigDialogComponent {
    partyConfig: PartyConfig;

    constructor(
        private readonly dialogRef: MatDialogRef<PartyConfigDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: PartyConfig,
    ) {
        // dialogRef.disableClose = true; // Deactivated for UX purpose.
        this.partyConfig = { ...data };
    }

    onCancel(): void {
        this.dialogRef.close(null);
    }

    onConfirm(): void {
        this.dialogRef.close(this.partyConfig);
    }
}
