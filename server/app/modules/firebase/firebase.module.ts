import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';

// REFERENCE: https://medium.com/@elangoram1998/getting-started-with-firebase-admin-in-nest-js-71f676e73e6
const firebaseProvider = {
    provide: 'FIREBASE_APP',
    inject: [ConfigService],
    useFactory: (configService: ConfigService) => {
        const firebaseConfig = {
            type: configService.get<string>('TYPE'),
            project_id: configService.get<string>('PROJECT_ID'),
            private_key_id: configService.get<string>('PRIVATE_KEY_ID'),
            private_key: configService.get<string>('PRIVATE_KEY').replace(/\\n/g, '\n'),
            client_email: configService.get<string>('CLIENT_EMAIL'),
            client_id: configService.get<string>('CLIENT_ID'),
            auth_uri: configService.get<string>('AUTH_URI'),
            token_uri: configService.get<string>('TOKEN_URI'),
            auth_provider_x509_cert_url: configService.get<string>('AUTH_CERT_URL'),
            client_x509_cert_url: configService.get<string>('CLIENT_CERT_URL'),
            universe_domain: configService.get<string>('UNIVERSAL_DOMAIN'),
        } as admin.ServiceAccount;
        firebaseConfig.projectId = 'log3900-201-7daa3'; // TODO: Import from .env properly
        return admin.initializeApp({
            credential: admin.credential.cert(firebaseConfig),
            databaseURL: `https://${firebaseConfig.projectId}-default-rtdb.firebaseio.com`,
            storageBucket: `${firebaseConfig.projectId}.firebasestorage.app`,
        });
    },
};

@Module({
    imports: [ConfigModule],
    providers: [firebaseProvider, FirebaseRepositoryService, FirebaseAuthService],
    exports: [FirebaseRepositoryService, FirebaseAuthService],
})
export class FirebaseModule {}
