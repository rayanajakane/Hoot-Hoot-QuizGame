import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { UserHistory } from '@common/interfaces/history-items';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class HistoryService extends CommunicationService<UserHistory> {
    constructor(http: HttpClient) {
        super(http, 'history');
    }

    getUserHistory(id: string): Observable<UserHistory> {
        return this.getById('', id);
    }
}
