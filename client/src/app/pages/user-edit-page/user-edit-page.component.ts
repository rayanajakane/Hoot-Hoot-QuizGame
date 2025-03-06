import { Component } from '@angular/core';
import { User } from '@angular/fire/auth';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAX_LENGTH, MIN_LENGTH } from '@app/constants/authentication';
import { IMAGE_MAX_FILE_SIZE, PresetAvatar } from '@app/constants/image-constants';
import { Language } from '@app/interfaces/language';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { Theme, ThemeService } from '@app/services/theme/theme.service';
import { TranslationService } from '@app/translation/translation.service';
import { TranslocoService } from '@jsverse/transloco';

export interface UserEditData {
    email: string;
    username: string;
    currentLang: string;
}
@Component({
    selector: 'app-user-edit-page',
    templateUrl: './user-edit-page.component.html',
    styleUrl: './user-edit-page.component.scss',
})
export class UserEditPageComponent {
    currentUser: User | null;
    isPresetAvatar = true; // TODO: Determine if we consider an existing avatar to be "preset"
    minUsernameLength = MIN_LENGTH;
    maxUsernameLength = MAX_LENGTH;

    initialAvatarUrl = this.authenticationService.userAvatarUrl;

    availableLangs: string[];
    availableThemes: Theme[];

    form = this.fb.group({
        email: [{ value: this.authenticationService.userEmail, disabled: true }],
        username: [
            this.authenticationService.userDisplayName,
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        avatar: [this.authenticationService.userAvatarUrl ? this.authenticationService.userAvatarUrl : PresetAvatar.Default],
        currentTheme: [this.themeService.currentTheme],
        currentLang: [this.translationService.currentLangugage],
    });

    // eslint-disable-next-line max-params
    constructor(
        public authenticationService: AuthenticationService,
        private fb: FormBuilder,
        public notificationService: NotificationService,
        private translocoService: TranslocoService,
        private translationService: TranslationService,
        private themeService: ThemeService,
    ) {
        this.availableLangs = this.translocoService.getAvailableLangs() as string[];
        this.availableThemes = this.themeService.getAvailableThemes() as Theme[];
        this.currentUser = this.authenticationService.currentUser;
    }

    get username() {
        return this.form.controls['username'];
    }

    get avatar() {
        return this.form.controls['avatar'];
    }

    get currentLang() {
        return this.form.controls['currentLang'];
    }

    get currentTheme() {
        return this.form.controls['currentTheme'];
    }

    get presetAvatar() {
        return PresetAvatar;
    }

    static isEmptyData(userEditData: UserEditData | undefined): boolean {
        return userEditData?.email === '' && userEditData.username === '' && userEditData.currentLang === null;
    }

    save() {
        this.form.markAllAsTouched();
        if (this.form.valid) {
            if (!this.isPresetAvatar && (this.avatar.value as string) !== this.initialAvatarUrl) {
                // TODO: TEMPORARY SOLUTION. Avatar should be uploaded in later commit.
                this.setPresetAvatar(PresetAvatar.Default);
            }
            this.translationService.setLanguage(this.currentLang.value as string);
            this.themeService.setTheme(this.currentTheme.value as Theme);

            this.authenticationService.editUserProfile(this.username.value as string, this.avatar.value as string);
            this.form.markAsPristine();
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

    deleteUser() {
        this.authenticationService.deleteUser();
    }

    onLanguageChange(language: Language) {
        this.translationService.setLanguage(language);
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
