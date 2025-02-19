import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'app-user-edit-page',
    templateUrl: './user-edit-page.component.html',
    styleUrl: './user-edit-page.component.scss',
})
export class UserEditPageComponent implements OnInit {
    hide = true;
    email = 'TODO';
    username = 'TODO';
    password = 'TODO';

    ngOnInit(): void {
        // TODO: Set email, username, and password based on userService
    }

    save() {
        // TODO
    }

    uploadAvatar() {
        // TODO
    }
}
