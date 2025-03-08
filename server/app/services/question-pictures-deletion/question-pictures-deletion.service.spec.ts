import { Test, TestingModule } from '@nestjs/testing';
import { QuestionPicturesDeletionService } from './question-pictures-deletion.service';

describe('QuestionPicturesDeletionService', () => {
  let service: QuestionPicturesDeletionService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [QuestionPicturesDeletionService],
    }).compile();

    service = module.get<QuestionPicturesDeletionService>(QuestionPicturesDeletionService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
