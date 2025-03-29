import { Test, TestingModule } from '@nestjs/testing';
import { EloGateway } from './elo.gateway';

describe('EloGateway', () => {
  let gateway: EloGateway;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [EloGateway],
    }).compile();

    gateway = module.get<EloGateway>(EloGateway);
  });

  it('should be defined', () => {
    expect(gateway).toBeDefined();
  });
});
