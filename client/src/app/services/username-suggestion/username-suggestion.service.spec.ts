import { TestBed } from '@angular/core/testing';

import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';

describe('UsernameSuggestionService', () => {
    let service: UsernameSuggestionService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(UsernameSuggestionService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
