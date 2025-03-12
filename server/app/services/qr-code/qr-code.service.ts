import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Injectable } from '@nestjs/common';
import * as qrcode from 'qrcode';

// REFERENCE: https://levelup.gitconnected.com/creating-a-qr-code-generator-api-with-nestjs-and-qrcode-js-af2e09617fa5
@Injectable()
export class QrCodeService {
    constructor(private firebaseRepositoryService: FirebaseRepositoryService) {}

    async generateQrCode(roomCode: string): Promise<string> {
        try {
            const qrCodeData = await qrcode.toDataURL(roomCode);
            const qrCodeUrl = await this.firebaseRepositoryService.uploadImage(`qrCodes/${roomCode}.png`, qrCodeData);
            return qrCodeUrl;
        } catch (error) {
            console.log(error);
        }
    }

    async deleteQrCode(roomCode: string) {
        try {
            await this.firebaseRepositoryService.deleteImage(`qrCodes/${roomCode}.png`);
        } catch (error) {
            console.log(error);
        }
    }
}
