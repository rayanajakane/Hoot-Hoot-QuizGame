import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { TranslocoService } from '@jsverse/transloco';

@Injectable({
    providedIn: 'root',
})
export class UsernameSuggestionService extends CommunicationService<string> {
    usernames: string[];
    constructor(
        http: HttpClient,
        private transloco: TranslocoService,
    ) {
        super(http, 'username-suggestion');
        this.usernames = [];
    }

    getUsernameSuggestions(): void {
        this.getAll(this.transloco.getActiveLang()).subscribe({
            next: (data: string[]) => {
                this.usernames = [...data];
            },
            error: (error: HttpErrorResponse) => console.log(error),
        });
    }
}
