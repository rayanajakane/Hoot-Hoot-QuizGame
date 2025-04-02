import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { UsernameSuggestionService } from './username-suggestion.service';

describe('UsernameSuggestionService', () => {
    let service: UsernameSuggestionService;
    let authService: SinonStubbedInstance<FirebaseAuthService>;

    beforeEach(async () => {
        authService = createStubInstance(FirebaseAuthService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                UsernameSuggestionService,
                {
                    provide: FirebaseAuthService,
                    useValue: authService,
                },
            ],
        }).compile();

        service = module.get<UsernameSuggestionService>(UsernameSuggestionService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
