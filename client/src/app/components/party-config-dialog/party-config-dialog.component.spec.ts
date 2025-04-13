import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { PartyConfigDialogComponent } from '@app/components/party-config-dialog/party-config-dialog.component';

describe('PartyConfigDialogComponent', () => {
    let component: PartyConfigDialogComponent;
    let fixture: ComponentFixture<PartyConfigDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PartyConfigDialogComponent],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: {} },
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PartyConfigDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
