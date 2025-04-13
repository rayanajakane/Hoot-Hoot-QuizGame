// cSpell:ignore Transloco jsverse TRANSLOCO TRANSPILER
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsListItemComponent } from '@app/components/friends-list-item/friends-list-item.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';

describe('FriendsListItemComponent', () => {
    let component: FriendsListItemComponent;
    let fixture: ComponentFixture<FriendsListItemComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FriendsListItemComponent],
            providers: [
                { provide: TranslocoService, useValue: jasmine.createSpyObj('TranslocoService', ['translate']) },
                { provide: 'TRANSLOCO_TRANSPILER', useValue: {} },
                { provide: AuthenticationService, useValue: {} },
            ],
        }).compileComponents();
        fixture = TestBed.createComponent(FriendsListItemComponent);
        component = fixture.componentInstance;
        component.user = {
            id: '1',
            photoUrl: 'https://example.com/avatar.png',
            name: 'Test User',
            isOnline: true,
        };
        fixture.detectChanges();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
