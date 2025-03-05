import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { ChatChannel } from '@app/constants/chat-channels';
import { PresetAvatar } from '@app/constants/image-constants';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
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

    // Allow more constructor parameters to decouple services
    // eslint-disable-next-line max-params
    constructor(
        readonly authenticationService: AuthenticationService,
        readonly chatService: ChatService,
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        private cdr: ChangeDetectorRef,
    ) {}

    get messages() {
        return this.chatService.channel === ChatChannel.GENERAL ? this.chatService.generalMessages : this.chatService.matchRoomMessages;
    }

    get channel() {
        return this.chatService.channel;
    }

    set channel(selectedChannel: string) {
        this.chatService.channel = selectedChannel;
    }

    ngAfterViewChecked() {
        this.scrollToBottom();
        this.cdr.detectChanges();
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

    public reactToMessage(messageId: string, chatEmoji: ChatEmoji) {
        this.chatService.reactToMessage(
            messageId,
            chatEmoji,
            this.authenticationService.userId,
            this.authenticationService.userDisplayName,
            this.matchRoomService.getRoomCode(),
        );
    }

    // REFERENCE: https://stackoverflow.com/questions/67600158/how-to-display-multiple-values-in-angular-material-tool-tip
    public getReactionsToolTip(userReactions: UserIdName[]) {
        let toolTip = '';
        for (let i = 0; i < userReactions.length; i++) {
            toolTip = toolTip + '\n' + userReactions[i].name;
        }
        return toolTip;
    }
}
