import { Component, OnInit } from '@angular/core';
import { User } from '@angular/fire/auth';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAX_LENGTH, MIN_LENGTH } from '@app/constants/authentication';
import { IMAGE_MAX_FILE_SIZE, PresetAvatar } from '@app/constants/image-constants';
import { Language } from '@app/interfaces/language';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { HistoryService } from '@app/services/history/history.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { TranslationService } from '@app/translation/translation.service';
import { UserHistory } from '@common/interfaces/history-items';
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
export class UserEditPageComponent implements OnInit {
    currentUser: User | null;
    isPresetAvatar = true; // TODO: Determine if we consider an existing avatar to be "preset"
    minUsernameLength = MIN_LENGTH;
    maxUsernameLength = MAX_LENGTH;
    loadedImageFile: File | null = null;

    availableLangs: string[];
    userHistory: UserHistory = {
        auth: [],
        match: [],
    };

    form = this.fb.group({
        email: [{ value: this.authenticationService.userEmail, disabled: true }],
        username: [
            this.authenticationService.userDisplayName,
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        avatar: [this.authenticationService.userAvatarUrl ? this.authenticationService.userAvatarUrl : PresetAvatar.Default],
        currentLang: [this.translationService.currentLangugage],
    });

    // eslint-disable-next-line max-params
    constructor(
        public authenticationService: AuthenticationService,
        private fb: FormBuilder,
        public notificationService: NotificationService,
        private translocoService: TranslocoService,
        private translationService: TranslationService,
        private historyService: HistoryService,
    ) {
        this.availableLangs = this.translocoService.getAvailableLangs() as string[];
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

    get presetAvatar() {
        return PresetAvatar;
    }

    async ngOnInit() {
        if (!this.currentUser) {
            this.userHistory = {
                auth: [],
                match: [],
            };
            return;
        }
        this.historyService.getUserHistory(this.currentUser.uid).subscribe({
            next: (userHistory: UserHistory) => {
                console.log(userHistory);
                this.userHistory = userHistory;
            },
            error: () => {
                this.userHistory = {
                    auth: [],
                    match: [],
                };
            },
        });
    }

    static isEmptyData(userEditData: UserEditData | undefined): boolean {
        return userEditData?.email === '' && userEditData.username === '' && userEditData.currentLang === null;
    }

    async save() {
        this.form.markAllAsTouched();
        if (this.form.valid) {
            var url: string = this.avatar.value as string;
            if (!this.isPresetAvatar && (this.avatar.value as string) !== this.authenticationService.userAvatarUrl) {
                const resultUrl = await this.authenticationService.uploadUserAvatar(this.authenticationService.userId, this.loadedImageFile);
                url = resultUrl !== '' ? resultUrl : this.authenticationService.userAvatarUrl;
            } else if (this.isPresetAvatar) {
                // Frees Firebase Storage space if user no longer needs uploaded avatar.
                this.authenticationService.deleteUserAvatar(this.authenticationService.userId);
            }
            // TODO: Consider adding the themes
            this.translationService.setLanguage(this.currentLang.value as string);

            this.authenticationService.editUserProfile(this.username.value as string, url);
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
