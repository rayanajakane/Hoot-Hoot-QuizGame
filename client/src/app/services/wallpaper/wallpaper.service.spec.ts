import { TestBed } from '@angular/core/testing';
import { authStub } from '@app/constants/auth-mocks';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { WallpaperService } from '@app/services/wallpaper/wallpaper.service';

describe('WallpaperService', () => {
    let service: WallpaperService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [WallpaperService, { provide: AuthenticationService, useValue: authStub }],
        });
        service = TestBed.inject(WallpaperService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
