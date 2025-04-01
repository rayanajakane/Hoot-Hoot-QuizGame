import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PresetAvatar } from '@app/constants/avatar-constants';
import { UserIdName } from '@common/interfaces/user-id-name';

@Component({
    selector: 'app-friends-list-item',
    templateUrl: './friends-list-item.component.html',
    styleUrls: ['./friends-list-item.component.scss'],
})
export class FriendsListItemComponent {
    defaultAvatar = PresetAvatar.Default;

    @Input() user: UserIdName;
    @Input() isFriend: boolean;
    @Input() isRequestPending: boolean;
    @Input() isRequestSent: boolean;
    @Input() isEligible: boolean;

    @Output() sendRequest = new EventEmitter<string>();
    @Output() cancelRequest = new EventEmitter<string>();
    @Output() acceptRequest = new EventEmitter<string>();
    @Output() rejectRequest = new EventEmitter<string>();
    @Output() removeFriend = new EventEmitter<string>();
    @Output() donate = new EventEmitter<string>();

    onSend(): void {
        this.sendRequest.emit(this.user.id);
    }

    onCancel(): void {
        this.cancelRequest.emit(this.user.id);
    }

    onAccept(): void {
        this.acceptRequest.emit(this.user.id);
    }

    onReject(): void {
        this.rejectRequest.emit(this.user.id);
    }

    onRemove(): void {
        this.removeFriend.emit(this.user.id);
    }

    onDonate(): void {
        this.donate.emit(this.user.id);
    }
}
