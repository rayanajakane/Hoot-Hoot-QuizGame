import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { HistoryService } from './history.service';

describe('HistoryService', () => {
    let service: HistoryService;
    let firebaseRepo: SinonStubbedInstance<FirebaseRepositoryService>;
    let firebaseAuth: SinonStubbedInstance<FirebaseAuthService>;

    beforeEach(async () => {
        firebaseRepo = createStubInstance(FirebaseRepositoryService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                HistoryService,
                { provide: FirebaseRepositoryService, useValue: firebaseRepo },
                { provide: FirebaseAuthService, useValue: firebaseAuth },
            ],
        }).compile();

        service = module.get<HistoryService>(HistoryService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
