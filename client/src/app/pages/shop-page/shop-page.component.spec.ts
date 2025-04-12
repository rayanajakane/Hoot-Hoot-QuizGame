import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';

import { ShopPageComponent } from '@app/pages/shop-page/shop-page.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { AvatarService } from '@app/services/avatar/avatar.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { Wallpaper, WallpaperService } from '@app/services/wallpaper/wallpaper.service';
import { of } from 'rxjs';

describe('ShopPageComponent', () => {
    let component: ShopPageComponent;
    let fixture: ComponentFixture<ShopPageComponent>;

    beforeEach(async () => {
        const wallpaperSpy = jasmine.createSpyObj('WallpaperService', [], {
            currentWallpaper$: of(Wallpaper.None),
        });

        await TestBed.configureTestingModule({
            imports: [],
            declarations: [ShopPageComponent],
            providers: [
                { provide: AuthenticationService, useValue: {} },
                { provide: AvatarService, useValue: {} },
                { provide: MoneyService, useValue: {} },
                { provide: NotificationService, useValue: {} },
                { provide: SocketHandlerService, useValue: {} },
                { provide: WallpaperService, useValue: wallpaperSpy },
                { provide: TranslocoService, useValue: {} },
                { provide: 'TRANSLOCO_TRANSPILER', useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ShopPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
