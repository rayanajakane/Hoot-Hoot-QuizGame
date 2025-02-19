import { TestBed } from '@angular/core/testing';

import { MatchRoomService } from '@app/services/match-room/match-room.service';

describe('MatchRoomService', () => {
    let service: MatchRoomService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(MatchRoomService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
