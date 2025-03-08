import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { GameService } from '../game/game.service';
import { QuestionService } from '../question/question.service';
import { QuestionPicturesDeletionService } from './question-pictures-deletion.service';

describe('QuestionPicturesDeletionService', () => {
    let service: QuestionPicturesDeletionService;
    let gameService: SinonStubbedInstance<GameService>;
    let questionService: SinonStubbedInstance<QuestionService>;
    let firebaseService: SinonStubbedInstance<FirebaseRepositoryService>;

    beforeEach(async () => {
        gameService = createStubInstance(GameService);
        questionService = createStubInstance(QuestionService);
        firebaseService = createStubInstance(FirebaseRepositoryService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                QuestionPicturesDeletionService,
                { provide: GameService, useValue: gameService },
                { provide: QuestionService, useValue: questionService },
                {
                    provide: FirebaseRepositoryService,
                    useValue: firebaseService,
                },
            ],
        }).compile();

        service = module.get<QuestionPicturesDeletionService>(QuestionPicturesDeletionService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
