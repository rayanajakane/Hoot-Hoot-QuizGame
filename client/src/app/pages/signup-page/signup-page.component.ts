import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAX_LENGTH, MIN_LENGTH, PW_MAX_LENGTH, PW_MIN_LENGTH } from '@app/constants/authentication';
import { IMAGE_MAX_FILE_SIZE, PresetAvatar } from '@app/constants/image-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TranslocoService } from '@jsverse/transloco';

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
    passwordMaxLength = PW_MAX_LENGTH;
    isPresetAvatar = true;
    loadedImageFile: File | null = null;

    form = this.fb.group({
        email: ['', { validators: [Validators.required, Validators.email], updateOn: 'blur' }],
        username: [
            '',
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        password: [
            '',
            {
                validators: [
                    Validators.required,
                    Validators.minLength(PW_MIN_LENGTH),
                    Validators.maxLength(PW_MAX_LENGTH),
                    this.passwordLowercaseValidator(),
                    this.passwordUppercaseValidator(),
                    this.passwordDigitValidator(),
                    this.passwordSpecialValidator(),
                ],
            },
        ],
        avatar: [PresetAvatar.Default],
    });

    constructor(
        private readonly authenticationService: AuthenticationService,
        public notificationService: NotificationService,
        private fb: FormBuilder,
        private readonly translocoService: TranslocoService,
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

    get avatar() {
        return this.form.controls['avatar'];
    }

    get presetAvatar() {
        return PresetAvatar;
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
            this.authenticationService.signUp(
                this.email.value as string,
                this.username.value as string,
                this.password.value as string,
                this.isPresetAvatar,
                this.avatar.value as string,
                this.loadedImageFile,
            );
        }
    }

    setCustomAvatar(event: Event): void {
        const eventTarget: HTMLInputElement | null = event.target as HTMLInputElement | null;
        if (eventTarget?.files?.[0]) {
            const file: File = eventTarget.files[0];
            if (file.size > IMAGE_MAX_FILE_SIZE) {
                this.notificationService.displayErrorMessage(this.translocoService.translate('auth.error.file-too-large'));
                return;
            }
            const reader = new FileReader();
            reader.addEventListener('load', () => {
                this.form.get('avatar')?.setValue(reader.result as null);
                this.loadedImageFile = file;
            });
            reader.readAsDataURL(file);
            this.isPresetAvatar = false;
        }
    }

    setPresetAvatar(presetAvatar: PresetAvatar) {
        this.isPresetAvatar = true;
        this.form.get('avatar')?.setValue(presetAvatar);
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

    private passwordLowercaseValidator(): ValidatorFn {
        return (passwordControl: AbstractControl): ValidationErrors | null => {
            const password = passwordControl.value as string;
            if (!password) {
                return null;
            }
            const containsLowercase = /(?=.*[a-z\u00E0-\u00FC])/.test(password);
            return containsLowercase ? null : { noLowercase: true };
        };
    }

    private passwordUppercaseValidator(): ValidatorFn {
        return (passwordControl: AbstractControl): ValidationErrors | null => {
            const password = passwordControl.value as string;
            if (!password) {
                return null;
            }
            const containsUppercase = /(?=.*[A-Z\u00C0-\u00DC])/.test(password);
            return containsUppercase ? null : { noUppercase: true };
        };
    }

    private passwordDigitValidator(): ValidatorFn {
        return (passwordControl: AbstractControl): ValidationErrors | null => {
            const password = passwordControl.value as string;
            if (!password) {
                return null;
            }
            const containsDigit = /(?=.*\d)/.test(password);
            return containsDigit ? null : { noDigit: true };
        };
    }

    private passwordSpecialValidator(): ValidatorFn {
        return (passwordControl: AbstractControl): ValidationErrors | null => {
            const password = passwordControl.value as string;
            if (!password) {
                return null;
            }
            // REFERENCE: Firebase special characters: https://firebase.google.com/docs/auth/web/password-auth
            const containsSpecial = /(?=.*[\^\$\*\.\[\]\{\}\(\)\?"!@#%&/\\,><':;\|_~])/.test(password);
            return containsSpecial ? null : { noSpecial: true };
        };
    }
}
