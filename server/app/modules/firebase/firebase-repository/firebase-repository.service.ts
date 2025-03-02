import { Inject, Injectable } from '@nestjs/common';
import { app } from 'firebase-admin';
import { Database } from 'firebase-admin/lib/database/database';

// REFERENCE: https://medium.com/@elangoram1998/getting-started-with-firebase-admin-in-nest-js-71f676e73e6
@Injectable()
export class FirebaseRepositoryService {
    db: FirebaseFirestore.Firestore;
    collection: FirebaseFirestore.CollectionReference;
    database: Database; // Realtime database

    constructor(@Inject('FIREBASE_APP') private firebaseApp: app.App) {
        this.db = firebaseApp.firestore();
        this.collection = this.db.collection('<collection_name>');
        this.database = firebaseApp.database('https://log3900-201-7daa3-default-rtdb.firebaseio.com/'); // TODO: Consider adding it to .env (?)
    }

    // Will be useful for friends & money
}
