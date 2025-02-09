import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ManagementState } from '@app/constants/states';
import { returnGuard } from '@app/guards/return-guard/return.guard';
import { AdminEditPageComponent } from '@app/pages/admin-edit-page/admin-edit-page.component';
import { AdminMainPageComponent } from '@app/pages/admin-main-page/admin-main-page.component';
import { AdminQuestionBankComponent } from '@app/pages/admin-question-bank/admin-question-bank.component';
import { ChatPageComponent } from '@app/pages/chat-page/chat-page.component';
import { LoginPageComponent } from '@app/pages/login-page/login-page.component';
import { SignupPageComponent } from '@app/pages/signup-page/signup-page.component';

const routes: Routes = [
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'home', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginPageComponent },
    { path: 'signup', component: SignupPageComponent },
    // TODO : Change guard condition
    { path: 'chat', component: ChatPageComponent },
    {
        path: 'admin',
        children: [
            { path: 'bank', component: AdminQuestionBankComponent },
            { path: 'games', component: AdminMainPageComponent },
            {
                path: 'games/new',
                component: AdminEditPageComponent,
                data: { state: ManagementState.GameCreate },
                canDeactivate: [returnGuard],
            },
            {
                path: 'games/:id',
                component: AdminEditPageComponent,
                data: { state: ManagementState.GameModify },
                canDeactivate: [returnGuard],
            },
        ],
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { useHash: true })],
    exports: [RouterModule],
})
export class AppRoutingModule {}
