import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { of } from 'rxjs';
import { ConfirmDialogComponent } from './confirm-dialog.component';

const mockData = { icon: 'warning', title: 'Title', text: 'Confirmation message', disableClose: true };

describe('ConfirmDialogComponent', () => {
    let component: ConfirmDialogComponent;
    let fixture: ComponentFixture<ConfirmDialogComponent>;
    let dialogRef: MatDialogRef<ConfirmDialogComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [MatDialogModule, MatIconModule],
            declarations: [ConfirmDialogComponent],
            providers: [
                {
                    provide: MatDialogRef,
                    useValue: { close: () => of(true) },
                },
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: mockData,
                },
            ],
        }).compileComponents();
        fixture = TestBed.createComponent(ConfirmDialogComponent);
        component = fixture.componentInstance;
        dialogRef = TestBed.inject(MatDialogRef);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize dialog with correct data', () => {
        expect(component.data).toEqual(mockData);
    });

    it('onCancel should close dialog with false', () => {
        const closeSpy = spyOn(dialogRef, 'close').and.stub();
        component.onCancel();
        expect(closeSpy).toHaveBeenCalledWith(false);
    });

    it('onConfirm should close dialog with true', () => {
        const closeSpy = spyOn(dialogRef, 'close').and.stub();
        component.onConfirm();
        expect(closeSpy).toHaveBeenCalledWith(true);
    });
});
