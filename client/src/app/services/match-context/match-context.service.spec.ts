import { TestBed } from '@angular/core/testing';

import { MatchContextService } from '@app/services/match-context/match-context.service';
describe('MatchContextService', () => {
    let service: MatchContextService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(MatchContextService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
