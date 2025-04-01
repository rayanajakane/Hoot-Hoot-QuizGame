import { DragDropModule } from '@angular/cdk/drag-drop';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSliderModule } from '@angular/material/slider';
import { MAT_TOOLTIP_DEFAULT_OPTIONS } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AlertComponent } from '@app/components/alert/alert.component';
import { ChatComponent } from '@app/components/chat/chat.component';
import { DialogConfirmComponent } from '@app/components/dialog-confirm/dialog-confirm.component';
import { DialogTextInputComponent } from '@app/components/dialog-text-input/dialog-text-input.component';
import { EstimatedAnswerAreaComponent } from '@app/components/estimated-answer-area/estimated-answer-area.component';
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
import { ForgotPasswordFeedbackPageComponent } from '@app/pages/forgot-password-feedback-page/forgot-password-feedback-page.component';
import { ForgotPasswordPageComponent } from '@app/pages/forgot-password-page/forgot-password-page.component';
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
import { FIREBASE_CONFIG } from 'src/environments/firebase-config';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { FriendsListItemComponent } from './components/friends-list-item/friends-list-item.component';
import { FriendsSearchComponent } from './components/friends-search/friends-search.component';
import { PartyConfigDialogComponent } from './components/party-config-dialog/party-config-dialog.component';
import { JoinMatchPageComponent } from './pages/join-match-page/join-match-page.component';
import { ShopPageComponent } from './pages/shop-page/shop-page.component';
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
        ForgotPasswordFeedbackPageComponent,
        SortByScorePipe,
        SortHistoryPipe,
        SortPlayersPipe,
        EstimatedAnswerAreaComponent,
        JoinMatchPageComponent,
        FriendsSearchComponent,
        FriendsListItemComponent,
        PartyConfigDialogComponent,
        ConfirmDialogComponent,
        ShopPageComponent,
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
        MatSliderModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        { provide: MAT_TOOLTIP_DEFAULT_OPTIONS, useValue: tooltipOptions },
        provideFirebaseApp(() => initializeApp(FIREBASE_CONFIG)),
        provideAuth(() => getAuth()),
    ],
    exports: [],
    bootstrap: [AppComponent],
})
export class AppModule {}
