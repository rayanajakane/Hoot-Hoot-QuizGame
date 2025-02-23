import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAX_LENGTH, MIN_LENGTH, PW_MIN_LENGTH } from '@app/constants/authentication';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

@Component({
    selector: 'app-signup-page',
    templateUrl: './signup-page.component.html',
    styleUrls: ['./signup-page.component.scss'],
})
export class SignupPageComponent implements OnInit {
    hide = true;
    minUsernameLength = MIN_LENGTH;
    maxUsernameLength = MAX_LENGTH;
    passwordMinLength = PW_MIN_LENGTH;

    form = this.fb.group({
        email: ['', { validators: [Validators.required, Validators.email], updateOn: 'blur' }],
        username: [
            '',
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        password: ['', { validators: [Validators.required, Validators.minLength(PW_MIN_LENGTH)] }],
    });

    constructor(
        private readonly authenticationService: AuthenticationService,
        private fb: FormBuilder,
    ) {}

    get email() {
        return this.form.controls['email'];
    }

    get username() {
        return this.form.controls['username'];
    }

    get password() {
        return this.form.controls['password'];
    }

    ngOnInit() {
        this.autofocus();
    }

    autofocus() {
        // REFERENCE: https://stackoverflow.com/questions/59893531/accessibilty-focus-is-lost-when-route-changed-in-angular
        const blurElement: HTMLElement = document.getElementById('email-input') as HTMLElement;
        blurElement?.blur();

        setTimeout(() => {
            const focusElement: HTMLElement = document.getElementById('email-input') as HTMLElement;
            focusElement?.focus();
        }, 0);
    }

    signUp() {
        this.form.markAllAsTouched();
        if (this.form.valid) {
            this.authenticationService.signUp(this.email.value as string, this.username.value as string, this.password.value as string);
        }
    }

    uploadAvatar() {
        // TODO
    }

    // TODO : Put in username service
    // https://blog.angular-university.io/angular-custom-validators/
    private usernameValidator(): ValidatorFn {
        return (usernameControl: AbstractControl): ValidationErrors | null => {
            const username = usernameControl.value as string;
            if (!username) {
                return null;
            }
            const containsSpecialChar = /[^A-Za-z0-9_]/.test(username);

            if (containsSpecialChar) {
                return { containsSpecialChar: true };
            }

            return null;
        };
    }
}
