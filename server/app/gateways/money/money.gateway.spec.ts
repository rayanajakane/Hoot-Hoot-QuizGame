import { Test, TestingModule } from '@nestjs/testing';
import { MoneyGateway } from './money.gateway';

describe('MoneyGateway', () => {
  let gateway: MoneyGateway;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [MoneyGateway],
    }).compile();

    gateway = module.get<MoneyGateway>(MoneyGateway);
  });

  it('should be defined', () => {
    expect(gateway).toBeDefined();
  });
});
