import { DragDropModule } from '@angular/cdk/drag-drop';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_TOOLTIP_DEFAULT_OPTIONS } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from '@app/modules/app-routing.module';
import { AppMaterialModule } from '@app/modules/material.module';
import { AppComponent } from '@app/pages/app/app.component';
import { ChatComponent } from './components/chat/chat.component';
import { tooltipOptions } from './constants/tooltip-options';
import { ClickStopPropagationDirective } from './directives/click-stop-propagation.directive';
import { ChatPageComponent } from './pages/chat-page/chat-page.component';
import { LoginPageComponent } from './pages/login-page/login-page.component';
import { SignupPageComponent } from './pages/signup-page/signup-page.component';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { TranslocoRootModule } from './transloco-root.module';
/**
 * Main module that is used in main.ts.
 * All automatically generated components will appear in this module.
 * Please do not move this module in the module folder.
 * Otherwise Angular Cli will not know in which module to put new component
 */
@NgModule({
    declarations: [AppComponent, ClickStopPropagationDirective, ChatComponent, ChatPageComponent, LoginPageComponent, SignupPageComponent],
    imports: [
        AppMaterialModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        DragDropModule,
        ReactiveFormsModule,
        TranslocoRootModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        { provide: MAT_TOOLTIP_DEFAULT_OPTIONS, useValue: tooltipOptions },
        provideFirebaseApp(() =>
            initializeApp({
                projectId: 'log3900-201-7daa3',
                appId: '1:4479204095:web:3b704c8df42da16ac2eaca',
                databaseURL: 'https://log3900-201-7daa3-default-rtdb.firebaseio.com',
                storageBucket: 'log3900-201-7daa3.firebasestorage.app',
                apiKey: 'AIzaSyBylwnS_bSV6_M5PORmlyS1vjgVr62Tr-s',
                authDomain: 'log3900-201-7daa3.firebaseapp.com',
                messagingSenderId: '4479204095',
            }),
        ),
        provideAuth(() => getAuth()),
    ],
    exports: [],
    bootstrap: [AppComponent],
})
export class AppModule {}
