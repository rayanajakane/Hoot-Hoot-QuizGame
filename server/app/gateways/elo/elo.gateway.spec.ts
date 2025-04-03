import { EloService } from '@app/services/elo/elo.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { EloGateway } from './elo.gateway';

describe('EloGateway', () => {
    let gateway: EloGateway;
    let eloService: SinonStubbedInstance<EloService>;

    beforeEach(async () => {
        eloService = createStubInstance(EloService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [EloGateway, { provide: EloService, useValue: eloService }],
        }).compile();

        gateway = module.get<EloGateway>(EloGateway);
    });

    it('should be defined', () => {
        expect(gateway).toBeDefined();
    });
});
