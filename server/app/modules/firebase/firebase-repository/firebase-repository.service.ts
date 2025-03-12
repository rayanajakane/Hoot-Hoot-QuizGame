import { Inject, Injectable } from '@nestjs/common';
import { app } from 'firebase-admin';
import { Database } from 'firebase-admin/lib/database/database';
import { Storage } from 'firebase-admin/lib/storage/storage';
import { getDownloadURL } from 'firebase-admin/storage';

// REFERENCE: https://medium.com/@elangoram1998/getting-started-with-firebase-admin-in-nest-js-71f676e73e6
@Injectable()
export class FirebaseRepositoryService {
    db: FirebaseFirestore.Firestore;
    collection: FirebaseFirestore.CollectionReference;
    database: Database; // Realtime database
    storage: Storage;

    constructor(@Inject('FIREBASE_APP') private firebaseApp: app.App) {
        this.db = firebaseApp.firestore();
        this.collection = this.db.collection('<collection_name>');
        this.database = firebaseApp.database('https://log3900-201-7daa3-default-rtdb.firebaseio.com/'); // TODO: Consider adding it to .env (?)
        this.storage = firebaseApp.storage();
    }

    // Reference: https://stackoverflow.com/questions/55111346/upload-file-to-firebase-storage-using-admin-sdk
    async uploadImage(path: string, imageLocalUrl: any): Promise<string> {
        const fileContent = await fetch(imageLocalUrl);
        const buffer = await fileContent.arrayBuffer();
        const bf = Buffer.from(buffer);

        const fileRef = this.storage.bucket().file(path);

        await fileRef.save(bf, {
            contentType: 'image/png',
        });

        const downloadUrl = await getDownloadURL(fileRef);
        return downloadUrl;
    }

    async deleteQuestionImage(url: string) {
        // Inspired by: https://stackoverflow.com/questions/47375945/delete-firebase-storage-image-url-with-download-url
        let name = url.substring(url.indexOf('%2F') + 3, url.indexOf('?'));
        name = name.replace('%20', ' ');
        await this.deleteImage(`questionPictures/${name}`);
    }

    async deleteImage(path: string) {
        try {
            await this.storage.bucket().file(path).delete();
        } catch (error) {
            // IMPORTANT to catch to avoid server crash
            console.log(error);
        }
    }

    // Will be useful for friends & money
}
