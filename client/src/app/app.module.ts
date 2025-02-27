import { DragDropModule } from '@angular/cdk/drag-drop';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_TOOLTIP_DEFAULT_OPTIONS } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AlertComponent } from '@app/components/alert/alert.component';
import { ChatComponent } from '@app/components/chat/chat.component';
import { DialogConfirmComponent } from '@app/components/dialog-confirm/dialog-confirm.component';
import { DialogTextInputComponent } from '@app/components/dialog-text-input/dialog-text-input.component';
import { GameListItemComponent } from '@app/components/game-list-item/game-list-item.component';
import { HistogramComponent } from '@app/components/histogram/histogram.component';
import { LongAnswerAreaComponent } from '@app/components/long-answer-area/long-answer-area.component';
import { LongAnswerHistogramComponent } from '@app/components/long-answer-histogram/long-answer-histogram.component';
import { MultipleChoiceAreaComponent } from '@app/components/multiple-choice-area/multiple-choice-area.component';
import { PlayersListComponent } from '@app/components/players-list/players-list.component';
import { PulseLoaderComponent } from '@app/components/pulse-loader/pulse-loader.component';
import { QuestionAreaComponent } from '@app/components/question-area/question-area.component';
import { QuestionCreationFormComponent } from '@app/components/question-creation-form/question-creation-form.component';
import { QuestionListItemComponent } from '@app/components/question-list-item/question-list-item.component';
import { ShortQuestionComponent } from '@app/components/short-question/short-question.component';
import { tooltipOptions } from '@app/constants/tooltip-options';
import { ClickStopPropagationDirective } from '@app/directives/click-stop-propagation.directive';
import { AppRoutingModule } from '@app/modules/app-routing.module';
import { AppMaterialModule } from '@app/modules/material.module';
import { AdminEditPageComponent } from '@app/pages/admin-edit-page/admin-edit-page.component';
import { AdminMainPageComponent } from '@app/pages/admin-main-page/admin-main-page.component';
import { AdminQuestionBankComponent } from '@app/pages/admin-question-bank/admin-question-bank.component';
import { AppComponent } from '@app/pages/app/app.component';
import { HomePageComponent } from '@app/pages/home-page/home-page.component';
import { LoginPageComponent } from '@app/pages/login-page/login-page.component';
import { MatchCreationPageComponent } from '@app/pages/match-creation-page/match-creation-page.component';
import { ResultsPageComponent } from '@app/pages/results-page/results-page.component';
import { SignupPageComponent } from '@app/pages/signup-page/signup-page.component';
import { UserEditPageComponent } from '@app/pages/user-edit-page/user-edit-page.component';
import { WaitPageComponent } from '@app/pages/wait-page/wait-page.component';
import { FilterByQuestionTypePipe } from '@app/pipes/filter-by-question-type.pipe';
import { SortAnswersPipe } from '@app/pipes/sort-answers.pipe';
import { SortByLastModificationPipe } from '@app/pipes/sort-by-last-modification.pipe';
import { SortByScorePipe } from '@app/pipes/sort-by-score.pipe';
import { SortHistoryPipe } from '@app/pipes/sort-history.pipe';
import { SortPlayersPipe } from '@app/pipes/sort-players.pipe';
import { TranslocoRootModule } from '@app/transloco-root.module';
import { ForgotPasswordPageComponent } from './pages/forgot-password-page/forgot-password-page.component';
import { ResetPasswordEmailSentPageComponent } from './pages/reset-password-email-sent-page/reset-password-email-sent-page.component';
/**
 * Main module that is used in main.ts.
 * All automatically generated components will appear in this module.
 * Please do not move this module in the module folder.
 * Otherwise Angular Cli will not know in which module to put new component
 */
@NgModule({
    declarations: [
        AppComponent,
        ClickStopPropagationDirective,
        AdminQuestionBankComponent,
        ShortQuestionComponent,
        QuestionCreationFormComponent,
        DialogConfirmComponent,
        AdminEditPageComponent,
        ChatComponent,
        LoginPageComponent,
        SignupPageComponent,
        DialogTextInputComponent,
        SortByLastModificationPipe,
        FilterByQuestionTypePipe,
        QuestionListItemComponent,
        AdminMainPageComponent,
        GameListItemComponent,
        HistogramComponent,
        LongAnswerAreaComponent,
        LongAnswerHistogramComponent,
        MultipleChoiceAreaComponent,
        PlayersListComponent,
        QuestionAreaComponent,
        AlertComponent,
        PulseLoaderComponent,
        HomePageComponent,
        MatchCreationPageComponent,
        ResultsPageComponent,
        WaitPageComponent,
        UserEditPageComponent,
        ForgotPasswordPageComponent,
        FilterByQuestionTypePipe,
        SortAnswersPipe,
        SortByLastModificationPipe,
        ResetPasswordEmailSentPageComponent,
        SortByScorePipe,
        SortHistoryPipe,
        SortPlayersPipe,
    ],
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
