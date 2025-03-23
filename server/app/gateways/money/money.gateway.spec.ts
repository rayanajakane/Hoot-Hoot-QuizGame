import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { MoneyService } from '@app/services/money/money.service';
import { Test, TestingModule } from '@nestjs/testing';
import { MoneyGateway } from './money.gateway';

describe('MoneyGateway', () => {
    let gateway: MoneyGateway;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                MoneyGateway,
                {
                    provide: MoneyService,
                    useValue: {
                        getCurrentBalance: jest.fn().mockResolvedValue(0),
                        updateBalance: jest.fn().mockResolvedValue(0),
                        donateMoney: jest.fn().mockResolvedValue(true),
                        getMoneyError: jest.fn().mockResolvedValue(null),
                    },
                },
                {
                    provide: FirebaseAuthService,
                    useValue: { getUserById: jest.fn().mockResolvedValue({ displayName: 'TestUser' }) },
                },
            ],
        }).compile();

        gateway = module.get<MoneyGateway>(MoneyGateway);
    });

    it('should be defined', () => {
        expect(gateway).toBeDefined();
    });
});
