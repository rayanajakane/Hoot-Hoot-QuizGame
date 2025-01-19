// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,
    serverUrl: 'http://localhost:3000/api',
    serverUrlWithoutApi: 'http://localhost:3000/',
    questionBankEndpoint: 'questions',
    firebase: {
        apiKey: 'AIzaSyBylwnS_bSV6_M5PORmlyS1vjgVr62Tr-s',
        authDomain: 'log3900-201-7daa3.firebaseapp.com',
        databaseURL: 'https://log3900-201-7daa3-default-rtdb.firebaseio.com',
        projectId: 'log3900-201-7daa3',
        storageBucket: 'log3900-201-7daa3.firebasestorage.app',
        messagingSenderId: '4479204095',
        appId: '1:4479204095:web:3b704c8df42da16ac2eaca',
    },
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
