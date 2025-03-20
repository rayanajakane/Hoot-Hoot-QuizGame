import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { FriendsService } from './friends.service';

describe('FriendsService', () => {
    let service: FriendsService;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            providers: [FriendsService, { provide: FirebaseRepositoryService, useValue: {} }, { provide: FirebaseAuthService, useValue: {} }],
        }).compile();

        service = module.get<FriendsService>(FriendsService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
