import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';
import { ChatEvents } from '@common/events/chat.events';

import { DisplayChatText } from '@app/constants/display-texts';

@Component({
    selector: 'app-chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements AfterViewChecked, OnInit, OnDestroy {
    @ViewChild('messagesContainer', { static: true }) messagesContainer: ElementRef;

    @Input() disableMessagingField: boolean;

    displayText = DisplayChatText;

    constructor(
        readonly authenticationService: AuthenticationService,
        readonly chatService: ChatService,
        private cdr: ChangeDetectorRef,
    ) {}

    ngOnInit(): void {
        this.authenticationService.connectToSocket();
        this.chatService.handleReceivedMessages();
    }

    ngAfterViewChecked() {
        this.scrollToBottom();
        this.cdr.detectChanges();
    }

    ngOnDestroy() {
        this.chatService.socketHandler.socket.removeListener(ChatEvents.NewMessage);
        this.authenticationService.disconnectSocket();
    }

    sendMessage(messageText: string): void {
        if (messageText) {
            const newMessage: Message = {
                text: messageText,
                author: this.authenticationService.userDisplayName,
                date: new Date(),
            };
            this.chatService.sendPrototypeMessage(newMessage);
        }
    }

    private scrollToBottom(): void {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }
}
