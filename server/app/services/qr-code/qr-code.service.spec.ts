import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Test, TestingModule } from '@nestjs/testing';
import { createStubInstance, SinonStubbedInstance } from 'sinon';
import { QrCodeService } from './qr-code.service';

describe('QrCodeService', () => {
    let service: QrCodeService;
    let firebaseSpy: SinonStubbedInstance<FirebaseRepositoryService>;

    beforeEach(async () => {
        firebaseSpy = createStubInstance(FirebaseRepositoryService);
        const module: TestingModule = await Test.createTestingModule({
            providers: [QrCodeService, { provide: FirebaseRepositoryService, useValue: firebaseSpy }],
        }).compile();

        service = module.get<QrCodeService>(QrCodeService);
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });
});
