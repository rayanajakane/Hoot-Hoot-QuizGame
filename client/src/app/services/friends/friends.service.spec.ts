import { TestBed } from '@angular/core/testing';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { TranslocoService } from '@jsverse/transloco';

import { FriendsService } from '@app/services/friends/friends.service';

describe('FriendsService', () => {
    let service: FriendsService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                FriendsService,
                { provide: TranslocoService, useValue: jasmine.createSpyObj('TranslocoService', ['translate']) },
                { provide: 'TRANSLOCO_TRANSPILER', useValue: {} },
                { provide: AuthenticationService, useValue: {} },
            ],
        });
        service = TestBed.inject(FriendsService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
