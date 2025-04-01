import { HttpClient, HttpHandler } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';
import { getTranslocoModule } from '@app/transloco-testing.module';

describe('UsernameSuggestionService', () => {
    let service: UsernameSuggestionService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [getTranslocoModule()],
            providers: [HttpClient, HttpHandler],
        });
        service = TestBed.inject(UsernameSuggestionService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
