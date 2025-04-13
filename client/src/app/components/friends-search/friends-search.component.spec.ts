import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsSearchComponent } from '@app/components/friends-search/friends-search.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { FriendsService } from '@app/services/friends/friends.service';
import { TranslocoService } from '@jsverse/transloco';

describe('FriendsSearchComponent', () => {
    let component: FriendsSearchComponent;
    let fixture: ComponentFixture<FriendsSearchComponent>;

    beforeEach(async () => {
        const mockFriendsService = {
            returnAllData: jasmine.createSpy('returnAllData'),
            searchUsers: jasmine.createSpy('searchUsers'),
            stopReturningUsers: jasmine.createSpy('stopReturningUsers'),
            searchResults: [],
            pendingRequests: [],
            sentRequests: [],
            friends: [],
            isFriend: () => false,
            isRequestPending: () => false,
            isRequestSent: () => false,
            isEligible: () => false,
            sendFriendRequest: jasmine.createSpy('sendFriendRequest'),
            cancelRequest: jasmine.createSpy('cancelRequest'),
            acceptFriendRequest: jasmine.createSpy('acceptFriendRequest'),
            rejectFriendRequest: jasmine.createSpy('rejectFriendRequest'),
            removeFriend: jasmine.createSpy('removeFriend'),
        };
        await TestBed.configureTestingModule({
            declarations: [FriendsSearchComponent],
            providers: [
                { provide: FriendsService, useValue: mockFriendsService },
                { provide: TranslocoService, useValue: jasmine.createSpyObj('TranslocoService', ['translate']) },
                { provide: 'TRANSLOCO_TRANSPILER', useValue: {} },
                { provide: AuthenticationService, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(FriendsSearchComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
