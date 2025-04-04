import { FriendsService } from '@app/services/friends/friends.service';
import { HistoryService } from '@app/services/history/history.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { FriendsGateway } from './friends.gateway';

describe('FriendsGateway', () => {
    let gateway: FriendsGateway;
    let historyService: SinonStubbedInstance<HistoryService>;

    beforeEach(async () => {
        historyService = createStubInstance(HistoryService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [FriendsGateway, { provide: FriendsService, useValue: {} }, { provide: HistoryService, useValue: historyService }],
        }).compile();

        gateway = module.get<FriendsGateway>(FriendsGateway);
    });

    it('should be defined', () => {
        expect(gateway).toBeDefined();
    });
});
