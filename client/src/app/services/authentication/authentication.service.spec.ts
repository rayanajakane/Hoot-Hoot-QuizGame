/* eslint-disable @typescript-eslint/no-explicit-any */
import { TestBed } from '@angular/core/testing';
import { authStub } from '@app/constants/auth-mocks';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { getTranslocoModule } from '@app/transloco-testing.module';

describe('AuthenticationService', () => {
    let service: AuthenticationService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            providers: [{ provide: AuthenticationService, useValue: authStub }],
        });
        service = TestBed.inject(AuthenticationService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
