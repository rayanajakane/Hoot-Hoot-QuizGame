import { Inject, Injectable } from '@nestjs/common';
import { app } from 'firebase-admin';
import { Auth } from 'firebase-admin/lib/auth/auth';
import { Database } from 'firebase-admin/lib/database/database';

// REFERENCE: https://medium.com/@elangoram1998/getting-started-with-firebase-admin-in-nest-js-71f676e73e6
@Injectable()
export class FirebaseRepository {
    #db: FirebaseFirestore.Firestore;
    #collection: FirebaseFirestore.CollectionReference;
    auth: Auth;
    database: Database;

    constructor(@Inject('FIREBASE_APP') private firebaseApp: app.App) {
        this.#db = firebaseApp.firestore();
        this.#collection = this.#db.collection('<collection_name>');
        this.auth = firebaseApp.auth();
        this.database = firebaseApp.database();
    }
}
