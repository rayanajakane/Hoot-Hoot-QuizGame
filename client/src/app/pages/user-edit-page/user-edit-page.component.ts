import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAX_LENGTH, MIN_LENGTH } from '@app/constants/authentication';
import { IMAGE_MAX_FILE_SIZE, PresetAvatar } from '@app/constants/image-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-user-edit-page',
    templateUrl: './user-edit-page.component.html',
    styleUrl: './user-edit-page.component.scss',
})
export class UserEditPageComponent {
    isPresetAvatar = true; // TODO: Determine if we consider an existing avatar to be "preset"
    minUsernameLength = MIN_LENGTH;
    maxUsernameLength = MAX_LENGTH;

    initialAvatarUrl = this.authenticationService.userAvatarUrl;

    form = this.fb.group({
        email: [{ value: this.authenticationService.userEmail, disabled: true }],
        username: [
            this.authenticationService.userDisplayName,
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        avatar: [this.authenticationService.userAvatarUrl],
    });

    constructor(
        public authenticationService: AuthenticationService,
        private fb: FormBuilder,
        public notificationService: NotificationService,
        private readonly translocoService: TranslocoService,
    ) {}

    get username() {
        return this.form.controls['username'];
    }

    get avatar() {
        return this.form.controls['avatar'];
    }

    get presetAvatar() {
        return PresetAvatar;
    }

    save() {
        this.form.markAllAsTouched();
        if (this.form.valid) {
            if (!this.isPresetAvatar && (this.avatar.value as string) != this.initialAvatarUrl) {
                // TODO: TEMPORARY SOLUTION. Avatar should be uploaded in later commit.
                this.setPresetAvatar(PresetAvatar.Default);
            }
            // TODO: Consider adding the themes + languages options when they are ready
            this.authenticationService.editUserProfile(this.username.value as string, this.avatar.value as string);
        }
    }

    // TODO: Consider refactoring later to avoid code repetition
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

    deleteUser() {
        this.authenticationService.deleteUser();
    }
}
