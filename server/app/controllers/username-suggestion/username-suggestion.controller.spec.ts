import { Test, TestingModule } from '@nestjs/testing';
import { UsernameSuggestionController } from './username-suggestion.controller';

describe('UsernameSuggestionController', () => {
    let controller: UsernameSuggestionController;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            controllers: [UsernameSuggestionController],
        }).compile();

        controller = module.get<UsernameSuggestionController>(UsernameSuggestionController);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });
});
