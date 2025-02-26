import { Component } from '@angular/core';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-user-edit-page',
    templateUrl: './user-edit-page.component.html',
    styleUrl: './user-edit-page.component.scss',
})
export class UserEditPageComponent {
    hide = true;
    email = 'TODO';
    username = 'TODO';
    password = 'TODO';

    constructor(public authenticationService: AuthenticationService) {}

    // ngOnInit(): void {
    // TODO: Set email, username, and password based on userService
    // }

    save() {
        // TODO
    }

    uploadAvatar() {
        // TODO
    }

    deleteUser() {
        this.authenticationService.deleteUser();
    }
}
