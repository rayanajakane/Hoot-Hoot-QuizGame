import { TestBed } from '@angular/core/testing';
import { authStub } from '@app/constants/auth-mocks';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { AvatarService } from '@app/services/avatar/avatar.service';

describe('AvatarService', () => {
    let service: AvatarService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [AvatarService, { provide: AuthenticationService, useValue: authStub }],
        });
        service = TestBed.inject(AvatarService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
