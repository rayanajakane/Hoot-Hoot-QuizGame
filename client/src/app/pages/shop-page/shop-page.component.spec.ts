import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { AvatarService } from '@app/services/avatar/avatar.service';
import { MoneyService } from '@app/services/money/money.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import { ShopPageComponent } from './shop-page.component';

describe('ShopPageComponent', () => {
    let component: ShopPageComponent;
    let fixture: ComponentFixture<ShopPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [],
            declarations: [ShopPageComponent],
            providers: [
                { provide: AuthenticationService, useValue: {} },
                { provide: AvatarService, useValue: {} },
                { provide: MoneyService, useValue: {} },
                { provide: NotificationService, useValue: {} },
                { provide: SocketHandlerService, useValue: {} },
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
