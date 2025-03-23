import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { MoneyService } from './money.service';

describe('MoneyService', () => {
    let service: MoneyService;

    beforeEach(async () => {
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
                { provide: FirebaseAuthService, useValue: {} },
            ],
        }).compile();

        service = module.get<MoneyService>(MoneyService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
