import { TestBed } from '@angular/core/testing';

import { getTranslocoModule } from '@app/transloco-testing.module';
import { AuthenticationService } from './authentication.service';

xdescribe('AuthenticationService', () => {
    let service: AuthenticationService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
        });
        service = TestBed.inject(AuthenticationService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
