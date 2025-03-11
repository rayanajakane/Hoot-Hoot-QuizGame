import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommunicationService } from '@app/services/communication/communication.service';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class FriendsService extends CommunicationService<any> {
    constructor(http: HttpClient) {
        super(http, 'friends');
    }

    getAllUsers(userId: string): Observable<UserIdName[]> {
        return this.getAll(`all/${userId}`);
    }

    getFriendsList(userId: string): Observable<UserIdName[]> {
        return this.getAll(`list/${userId}`);
    }

    getPendingRequests(userId: string): Observable<UserIdName[]> {
        return this.getAll(`requests/pending/${userId}`);
    }

    getSentRequests(userId: string): Observable<UserIdName[]> {
        return this.getAll(`requests/sent/${userId}`);
    }

    cancelRequest(userId: string, friendId: string): Observable<void> {
        return this.delete(`cancel/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    sendFriendRequest(fromUserId: string, toUserId: string): Observable<void> {
        return this.add({}, `send/${fromUserId}/${toUserId}`).pipe(map(() => undefined));
    }

    acceptFriendRequest(userId: string, friendId: string): Observable<void> {
        return this.add({}, `accept/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    rejectFriendRequest(userId: string, friendId: string): Observable<void> {
        return this.add({}, `reject/${userId}/${friendId}`).pipe(map(() => undefined));
    }

    removeFriend(userId: string, friendId: string): Observable<void> {
        return this.delete(`remove/${userId}/${friendId}`).pipe(map(() => undefined));
    }
}
