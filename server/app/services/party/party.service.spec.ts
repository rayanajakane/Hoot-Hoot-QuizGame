import { FriendsService } from '@app/services/friends/friends.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MoneyService } from '@app/services/money/money.service';
import { PartyService } from '@app/services/party/party.service';
import { Test, TestingModule } from '@nestjs/testing';
import { SinonStubbedInstance, createStubInstance } from 'sinon';

describe('PartyService', () => {
    let service: PartyService;
    let moneySpy: SinonStubbedInstance<MoneyService>;
    let friendSpy: SinonStubbedInstance<FriendsService>;
    let matchRoomSpy: SinonStubbedInstance<MatchRoomService>;

    beforeEach(async () => {
        moneySpy = createStubInstance(MoneyService);
        friendSpy = createStubInstance(FriendsService);
        matchRoomSpy = createStubInstance(MatchRoomService);

        const module: TestingModule = await Test.createTestingModule({
            providers: [
                PartyService,
                { provide: MoneyService, useValue: moneySpy },
                { provide: FriendsService, useValue: friendSpy },
                { provide: MatchRoomService, useValue: matchRoomSpy },
            ],
        }).compile();

        service = module.get<PartyService>(PartyService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
