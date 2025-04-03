import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { MatchRoomService } from '../match-room/match-room.service';
import { EloService } from './elo.service';

describe('EloService', () => {
    let service: EloService;
    let authService: SinonStubbedInstance<FirebaseAuthService>;
    let repoService: SinonStubbedInstance<FirebaseRepositoryService>;
    let matchRoomService: SinonStubbedInstance<MatchRoomService>;

    beforeEach(async () => {
        authService = createStubInstance(FirebaseAuthService);
        repoService = createStubInstance(FirebaseRepositoryService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                EloService,
                { provide: FirebaseAuthService, useValue: authService },
                { provide: FirebaseRepositoryService, useValue: repoService },
                { provide: MatchRoomService, useValue: matchRoomService },
            ],
        }).compile();

        service = module.get<EloService>(EloService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
