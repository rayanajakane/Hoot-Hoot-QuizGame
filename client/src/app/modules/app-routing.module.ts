import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FriendsSearchComponent } from '@app/components/friends-search/friends-search.component';
import { QuestionAreaComponent } from '@app/components/question-area/question-area.component';
import { ManagementState } from '@app/constants/states';
import { authenticationGuard } from '@app/guards/authentication/authentication.guard';
import { matchLoginGuard } from '@app/guards/match-login/match-login.guard';
import { AdminEditPageComponent } from '@app/pages/admin-edit-page/admin-edit-page.component';
import { AdminMainPageComponent } from '@app/pages/admin-main-page/admin-main-page.component';
import { AdminQuestionBankComponent } from '@app/pages/admin-question-bank/admin-question-bank.component';
import { ForgotPasswordFeedbackPageComponent } from '@app/pages/forgot-password-feedback-page/forgot-password-feedback-page.component';
import { ForgotPasswordPageComponent } from '@app/pages/forgot-password-page/forgot-password-page.component';
import { HomePageComponent } from '@app/pages/home-page/home-page.component';
import { JoinMatchPageComponent } from '@app/pages/join-match-page/join-match-page.component';
import { LoginPageComponent } from '@app/pages/login-page/login-page.component';
import { MatchCreationPageComponent } from '@app/pages/match-creation-page/match-creation-page.component';
import { ResultsPageComponent } from '@app/pages/results-page/results-page.component';
import { ShopPageComponent } from '@app/pages/shop-page/shop-page.component';
import { SignupPageComponent } from '@app/pages/signup-page/signup-page.component';
import { UserEditPageComponent } from '@app/pages/user-edit-page/user-edit-page.component';
import { WaitPageComponent } from '@app/pages/wait-page/wait-page.component';

const routes: Routes = [
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'home', component: HomePageComponent, canActivate: [authenticationGuard] },
    { path: 'login', component: LoginPageComponent },
    { path: 'signup', component: SignupPageComponent },
    { path: 'forgot-password', component: ForgotPasswordPageComponent },
    {
        path: 'admin',
        canActivate: [authenticationGuard],
        children: [
            { path: 'bank', component: AdminQuestionBankComponent },
            { path: 'games', component: AdminMainPageComponent },
            {
                path: 'games/new',
                component: AdminEditPageComponent,
                data: { state: ManagementState.GameCreate },
            },
            {
                path: 'games/:id',
                component: AdminEditPageComponent,
                data: { state: ManagementState.GameModify },
            },
        ],
    },
    { path: 'host', component: MatchCreationPageComponent, canActivate: [authenticationGuard] },
    { path: 'match-room', canActivate: [matchLoginGuard], component: WaitPageComponent },
    { path: 'join-match', canActivate: [authenticationGuard], component: JoinMatchPageComponent },
    { path: 'play-match', canActivate: [matchLoginGuard], component: QuestionAreaComponent },
    { path: 'results', canActivate: [matchLoginGuard], component: ResultsPageComponent },
    { path: 'edit-profile', component: UserEditPageComponent, canActivate: [authenticationGuard] },
    { path: 'reset-password-email-sent', component: ForgotPasswordFeedbackPageComponent },
    { path: 'friends-search', component: FriendsSearchComponent, canActivate: [authenticationGuard] },
    { path: 'shop', component: ShopPageComponent, canActivate: [authenticationGuard] },
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { useHash: true, scrollPositionRestoration: 'enabled' })],
    exports: [RouterModule],
})
export class AppRoutingModule {}
