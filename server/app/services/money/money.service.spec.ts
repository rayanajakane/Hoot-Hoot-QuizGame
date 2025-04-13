import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance } from 'sinon';
import { FriendsService } from '../friends/friends.service';
import { MoneyService } from './money.service';

describe('MoneyService', () => {
    let service: MoneyService;
    let firebaseAuthSpy: SinonStubbedInstance<FirebaseAuthService>;
    let matchRoomSpy: SinonStubbedInstance<MatchRoomService>;
    let friendsSpy: SinonStubbedInstance<FriendsService>;

    beforeEach(async () => {
        firebaseAuthSpy = createStubInstance(FirebaseAuthService);
        matchRoomSpy = createStubInstance(MatchRoomService);
        friendsSpy = createStubInstance(FriendsService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                MoneyService,
                {
                    provide: FirebaseRepositoryService,
                    useValue: {
                        database: {
                            ref: jest.fn(() => ({
                                once: jest.fn().mockResolvedValue({ exists: () => false, val: () => 0 }),
                                set: jest.fn(),
                                push: jest.fn(() => ({ set: jest.fn() })),
                            })),
                        },
                    },
                },
                { provide: FirebaseAuthService, useValue: firebaseAuthSpy },
                { provide: MatchRoomService, useValue: matchRoomSpy },
                { provide: FriendsService, useValue: friendsSpy },
            ],
        }).compile();

        service = module.get<MoneyService>(MoneyService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
