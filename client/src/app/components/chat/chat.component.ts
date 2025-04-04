import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { PresetAvatar } from '@app/constants/avatar-constants';
import { ChatChannel } from '@app/constants/chat-channels';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Wallpaper, WallpaperService } from '@app/services/wallpaper/wallpaper.service';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { UserIdName } from '@common/interfaces/user-id-name';

@Component({
    selector: 'app-chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements AfterViewChecked {
    @ViewChild('messagesContainer', { static: true }) messagesContainer: ElementRef;

    defaultAvatar = PresetAvatar.Default;
    emoji = ChatEmoji;
    currentWallpaper: string = Wallpaper.None;

    // Allow more constructor parameters to decouple services
    // eslint-disable-next-line max-params
    constructor(
        readonly authenticationService: AuthenticationService,
        readonly chatService: ChatService,
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        private wallpaperService: WallpaperService,
        private cdr: ChangeDetectorRef,
    ) {
        this.wallpaperService.currentWallpaper$.subscribe((wallpaper) => {
            this.currentWallpaper = wallpaper;
        });
    }

    get messages() {
        return this.chatService.channel === ChatChannel.GENERAL ? this.chatService.generalMessages : this.chatService.matchRoomMessages;
    }

    get channel() {
        return this.chatService.channel;
    }

    set channel(selectedChannel: string) {
        this.chatService.channel = selectedChannel;
    }

    hasBackground(): boolean {
        return this.currentWallpaper !== Wallpaper.None;
    }

    ngAfterViewChecked() {
        this.chatService.updateChatScroll.subscribe(() => {
            this.cdr.detectChanges();
            this.scrollToBottom();
        });
    }

    sendMessage(messageText: string): void {
        if (!messageText) {
            return;
        }
        const newMessage: Message = {
            id: '',
            text: messageText,
            authorId: this.authenticationService.userId,
            authorUsername: this.authenticationService.userDisplayName,
            photoUrl: this.authenticationService.userAvatarUrl,
            date: new Date(),
            userLikes: [],
            userLoves: [],
            userDislikes: [],
        };
        this.chatService.sendMessage(newMessage, this.matchRoomService.getRoomCode());
    }

    private scrollToBottom(): void {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }

    reactToMessage(messageId: string, chatEmoji: ChatEmoji) {
        this.chatService.reactToMessage(
            messageId,
            chatEmoji,
            this.authenticationService.userId,
            this.authenticationService.userDisplayName,
            this.matchRoomService.getRoomCode(),
        );
    }

    // REFERENCE: https://stackoverflow.com/questions/67600158/how-to-display-multiple-values-in-angular-material-tool-tip
    getReactionsToolTip(userReactions: UserIdName[]) {
        let toolTip = '';
        for (let i = 0; i < userReactions.length; i++) {
            toolTip = toolTip + '\n' + userReactions[i].name;
        }
        return toolTip;
    }

    isOwnReaction(userReactions: UserIdName[]) {
        return userReactions.find((it: UserIdName) => it.id === this.authenticationService.userId) ? true : false;
    }
}
