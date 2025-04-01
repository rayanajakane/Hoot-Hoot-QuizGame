import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { UsernameSuggestionController } from './username-suggestion.controller';

describe('UsernameSuggestionController', () => {
    let controller: UsernameSuggestionController;
    let suggestionService: SinonStubbedInstance<UsernameSuggestionService>;

    beforeEach(async () => {
        suggestionService = createStubInstance(UsernameSuggestionService);
        const module: TestingModule = await Test.createTestingModule({
            controllers: [UsernameSuggestionController],
            providers: [
                {
                    provide: UsernameSuggestionService,
                    useValue: suggestionService,
                },
            ],
        }).compile();

        controller = module.get<UsernameSuggestionController>(UsernameSuggestionController);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });
});
