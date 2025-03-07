import { MOCK_PLAYER_ROOM } from '@app/constants/match-mocks';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { EventEmitter2 } from '@nestjs/event-emitter';
import { Test, TestingModule } from '@nestjs/testing';
import { EstimatedAnswerStrategy } from './estimated-answer-strategy';

describe('EstimatedAnswerStrategy', () => {
    let strategy: EstimatedAnswerStrategy;
    let matchRoom: MatchRoom;
    let mockHostSocket;
    let mockPlayerSocket;
    let eventEmitterMock;

    const currentTime = 100000;

    beforeEach(async () => {
        eventEmitterMock = {
            emit: jest.fn(),
        };

        const module: TestingModule = await Test.createTestingModule({
            providers: [EstimatedAnswerStrategy, { provide: EventEmitter2, useValue: eventEmitterMock }],
        }).compile();

        mockHostSocket = {
            emit: jest.fn(),
        };

        mockPlayerSocket = {
            emit: jest.fn(),
        };

        strategy = module.get<EstimatedAnswerStrategy>(EstimatedAnswerStrategy);

        matchRoom = { ...MOCK_PLAYER_ROOM };
        matchRoom.hostSocket = mockHostSocket;

        jest.spyOn<any, any>(Date, 'now').mockReturnValue(currentTime);
    });

    it('should be defined', () => {
        expect(strategy).toBeDefined();
    });
});
