import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { ChatChannel } from '@app/constants/chat-channels';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';

@Component({
    selector: 'app-chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements AfterViewChecked {
    @ViewChild('messagesContainer', { static: true }) messagesContainer: ElementRef;

    get channel() {
        return this.chatService.channel;
    }

    set channel(selectedChannel: string) {
        this.chatService.channel = selectedChannel;
    }

    get messages() {
        return this.chatService.channel === ChatChannel.GENERAL ? this.chatService.generalMessages : this.chatService.matchRoomMessages;
    }

    constructor(
        readonly authenticationService: AuthenticationService,
        readonly chatService: ChatService,
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        private cdr: ChangeDetectorRef,
    ) {}

    ngAfterViewChecked() {
        this.scrollToBottom();
        this.cdr.detectChanges();
    }

    sendMessage(messageText: string): void {
        if (!messageText) {
            return;
        }
        const newMessage: Message = {
            text: messageText,
            author: this.authenticationService.userDisplayName,
            date: new Date(),
        };
        this.chatService.sendMessage(newMessage, this.matchRoomService.getRoomCode());
    }

    private scrollToBottom(): void {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }
}
