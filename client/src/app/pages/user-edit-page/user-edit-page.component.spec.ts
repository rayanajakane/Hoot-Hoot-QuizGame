import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCard } from '@angular/material/card';
import { MatOption } from '@angular/material/core';
import { MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatSelect } from '@angular/material/select';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { HistoryService } from '@app/services/history/history.service';
import { Wallpaper, WallpaperService } from '@app/services/wallpaper/wallpaper.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { of } from 'rxjs';
import { UserEditPageComponent } from './user-edit-page.component';

describe('UserEditPageComponent', () => {
    let component: UserEditPageComponent;
    let fixture: ComponentFixture<UserEditPageComponent>;

    beforeEach(async () => {
        const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['deleteUser']);
        const historySpy = jasmine.createSpyObj('HistoryService', ['getUserHistory']);
        const wallpaperSpy = jasmine.createSpyObj('WallpaperService', [], {
            currentWallpaper$: of(Wallpaper.None),
        });
        await TestBed.configureTestingModule({
            imports: [getTranslocoModule(), ReactiveFormsModule, MatOption, MatSelect, MatLabel, MatIcon, MatCard],
            declarations: [UserEditPageComponent],
            providers: [
                { provide: AuthenticationService, useValue: authenticationSpy },
                { provide: HistoryService, useValue: historySpy },
                { provide: WallpaperService, useValue: wallpaperSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(UserEditPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
