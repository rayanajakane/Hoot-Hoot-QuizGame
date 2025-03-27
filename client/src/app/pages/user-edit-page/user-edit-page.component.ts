import { Component, OnInit } from '@angular/core';
import { User } from '@angular/fire/auth';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '@app/components/confirm-dialog/confirm-dialog.component';
import { MAX_LENGTH, MIN_LENGTH } from '@app/constants/authentication';
import { IMAGE_MAX_FILE_SIZE, PresetAvatar } from '@app/constants/image-constants';
import { Language } from '@app/interfaces/language';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { HistoryService } from '@app/services/history/history.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { Theme, ThemeService } from '@app/services/theme/theme.service';
import { TranslationService } from '@app/translation/translation.service';
import { UserHistoryInfo } from '@common/interfaces/history-items';
import { translate, TranslocoService } from '@jsverse/transloco';

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
    isPresetAvatar = true;
    minUsernameLength = MIN_LENGTH;
    maxUsernameLength = MAX_LENGTH;
    loadedImageFile: File | null = null;

    availableLangs: Language[];
    availableThemes = Object.values(Theme);
    themeLabels = {
        [Theme.DARK]: translate('page.dark-theme'),
        [Theme.LIGHT]: translate('page.light-theme'),
    };
    langLabels = {
        ['fr']: translate('page.fr'),
        ['en']: translate('page.en'),
    };
    userHistory: UserHistoryInfo = {
        auth: [],
        match: [],
        stats: {
            nMatchesPlayed: 0,
            nMatchesWon: 0,
            averageGoodAnswersPercentage: 0,
            averageTime: 0,
        },
        intensityGrid: Array(365).fill(0),
    };

    form = this.fb.group({
        email: [{ value: this.authenticationService.userEmail, disabled: true }],
        username: [
            this.authenticationService.userDisplayName,
            { validators: [Validators.required, Validators.minLength(MIN_LENGTH), Validators.maxLength(MAX_LENGTH), this.usernameValidator()] },
        ],
        avatar: [this.authenticationService.userAvatarUrl ? this.authenticationService.userAvatarUrl : PresetAvatar.Default],
        currentLang: [this.translationService.currentLangugage],
        currentTheme: [this.themeService.currentTheme],
    });

    // eslint-disable-next-line max-params
    constructor(
        public authenticationService: AuthenticationService,
        private fb: FormBuilder,
        public notificationService: NotificationService,
        private translocoService: TranslocoService,
        private translationService: TranslationService,
        private themeService: ThemeService,
        private historyService: HistoryService,
        public dialog: MatDialog,
    ) {
        this.availableLangs = this.translationService.getAllLanguages();
        this.availableThemes = this.themeService.getAvailableThemes() as Theme[];

        this.currentUser = this.authenticationService.currentUser;
    }

    get currentTheme() {
        return this.form.controls['currentTheme'];
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

    getThemeLabel(theme: Theme): string {
        return this.themeLabels[theme];
    }

    getLangLabel(lang: 'fr' | 'en'): string {
        return this.langLabels[lang];
    }
    async ngOnInit() {
        if (!this.currentUser) {
            this.userHistory = {
                auth: [],
                match: [],
                stats: {
                    nMatchesPlayed: 0,
                    nMatchesWon: 0,
                    averageGoodAnswersPercentage: 0,
                    averageTime: 0,
                },
                intensityGrid: Array(365).fill(0),
            };
            return;
        }
        this.historyService.getUserHistory(this.currentUser.uid).subscribe({
            next: (userHistory: UserHistoryInfo) => {
                this.userHistory = userHistory;
            },
            error: (error) => {
                this.userHistory = {
                    auth: [],
                    match: [],
                    stats: {
                        nMatchesPlayed: 0,
                        nMatchesWon: 0,
                        averageGoodAnswersPercentage: 0,
                        averageTime: 0,
                    },
                    intensityGrid: Array(365).fill(0),
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
            let url: string = this.avatar.value as string;
            if (!this.isPresetAvatar && (this.avatar.value as string) !== this.authenticationService.userAvatarUrl) {
                const resultUrl = await this.authenticationService.uploadUserAvatar(this.authenticationService.userId, this.loadedImageFile);
                url = resultUrl !== '' ? resultUrl : this.authenticationService.userAvatarUrl;
            } else if (this.isPresetAvatar) {
                // Frees Firebase Storage space if user no longer needs uploaded avatar.
                this.authenticationService.deleteUserAvatar(this.authenticationService.userId);
            }
            this.themeService.setTheme(this.currentTheme.value as Theme);
            this.translationService.setLanguage(this.currentLang.value as string);

            // Reset labels to new language
            this.themeLabels = {
                [Theme.DARK]: translate('page.dark-theme'),
                [Theme.LIGHT]: translate('page.light-theme'),
            };

            this.langLabels = {
                ['fr']: translate('page.fr'),
                ['en']: translate('page.en'),
            };
            this.authenticationService.editUserProfile(this.username.value as string, url);
            this.form.markAsPristine();
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

    openDeleteDialog() {
        const data = {
            icon: 'warning',
            title: 'common.warning',
            text: 'page.delete-warning',
        };
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            data,
        });
        dialogRef.afterClosed().subscribe((confirm) => {
            if (confirm) {
                this.deleteUser();
            }
        });
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
