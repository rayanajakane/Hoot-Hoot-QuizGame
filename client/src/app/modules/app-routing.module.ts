import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { useHash: true })],
    exports: [RouterModule],
})
export class AppRoutingModule {}
