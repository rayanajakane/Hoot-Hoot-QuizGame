import { TestBed } from '@angular/core/testing';

import { AvatarService } from '@app/services/avatar/avatar.service';

describe('AvatarService', () => {
    let service: AvatarService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(AvatarService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
