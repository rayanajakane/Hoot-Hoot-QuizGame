import { Test, TestingModule } from '@nestjs/testing';
import { UsernameSuggestionService } from './username-suggestion.service';

describe('UsernameSuggestionService', () => {
    let service: UsernameSuggestionService;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            providers: [UsernameSuggestionService],
        }).compile();

        service = module.get<UsernameSuggestionService>(UsernameSuggestionService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
