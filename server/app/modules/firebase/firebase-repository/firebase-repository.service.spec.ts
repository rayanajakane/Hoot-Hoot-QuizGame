import { Test, TestingModule } from '@nestjs/testing';
import { FirebaseRepositoryService } from './firebase-repository.service';

describe('FirebaseRepositoryService', () => {
  let service: FirebaseRepositoryService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [FirebaseRepositoryService],
    }).compile();

    service = module.get<FirebaseRepositoryService>(FirebaseRepositoryService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
