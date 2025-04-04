import { DatabaseReference } from 'firebase/database';
import { BehaviorSubject } from 'rxjs';

// Ref : https://github.com/angular/angularfire/discussions/18
export interface UserMock {
    uid: string;
    email: string;
}

export const mockUser: UserMock = {
    uid: '1234',
    email: 'caca@polyquiz.com',
};

export const fakeAuthState = new BehaviorSubject<UserMock | null>(mockUser); // <= Pay attention to this guy

export const fakeSignInHandler = async (email: string, password: string): Promise<UserMock> => {
    fakeAuthState.next(mockUser);
    return Promise.resolve(mockUser);
};

export const fakeSignOutHandler = async (): Promise<UserMock | null> => {
    fakeAuthState.next(null);
    return Promise.resolve(null);
};

const getUserDatabaseRefMock = async (): Promise<DatabaseReference> => {
    return Promise.resolve({} as DatabaseReference);
};

export const authStub = {
    authState: fakeAuthState,
    auth: {
        createUserWithEmailAndPassword: jasmine.createSpy('createUserWithEmailAndPassword').and.callFake(fakeSignInHandler),
        signInWithEmailAndPassword: jasmine.createSpy('signInWithEmailAndPassword').and.callFake(fakeSignInHandler),
        signOut: jasmine.createSpy('signOut').and.callFake(fakeSignOutHandler),
    },
    getUserDatabaseRef: jasmine.createSpy('getUserDatabaseRef').and.callFake(getUserDatabaseRefMock),
    // getThemeFromDB: jasmine.createSpy('getThemeFromDB').and.returnValue(Theme.DARK),
};
