import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { UserHistoryInfo } from '@common/interfaces/history-items';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class HistoryService extends CommunicationService<UserHistoryInfo> {
    constructor(http: HttpClient) {
        super(http, 'history');
    }

    getUserHistory(id: string): Observable<UserHistoryInfo> {
        return this.getById('', id);
    }
}
