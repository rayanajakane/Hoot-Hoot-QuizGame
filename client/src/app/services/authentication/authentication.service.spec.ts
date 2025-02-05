import { TestBed } from '@angular/core/testing';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { BehaviorSubject } from 'rxjs';
import { AuthenticationService } from './authentication.service';

describe('AuthenticationService', () => {
    let service: AuthenticationService;

    // Ref : https://github.com/angular/angularfire/discussions/18
    interface UserMock {
        uid: string;
        email: string;
    }

    const user: UserMock = {
        uid: '1234',
        email: 'caca@polyquiz.com',
    };

    const fakeAuthState = new BehaviorSubject<UserMock | null>(user); // <= Pay attention to this guy

    const fakeSignInHandler = async (email: string, password: string): Promise<UserMock> => {
        fakeAuthState.next(user);
        return Promise.resolve(user);
    };

    const fakeSignOutHandler = async (): Promise<UserMock | null> => {
        fakeAuthState.next(null);
        return Promise.resolve(null);
    };

    const authStub = {
        authState: fakeAuthState,
        auth: {
            createUserWithEmailAndPassword: jasmine.createSpy('createUserWithEmailAndPassword').and.callFake(fakeSignInHandler),
            signInWithEmailAndPassword: jasmine.createSpy('signInWithEmailAndPassword').and.callFake(fakeSignInHandler),
            signOut: jasmine.createSpy('signOut').and.callFake(fakeSignOutHandler),
        },
    };

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
