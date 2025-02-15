import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, Input, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { ChatService } from '@app/services/chat/chat.service';

@Component({
    selector: 'app-chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements AfterViewChecked {
    @ViewChild('messagesContainer', { static: true }) messagesContainer: ElementRef;

    @Input() disableMessagingField: boolean;

    constructor(
        readonly authenticationService: AuthenticationService,
        readonly chatService: ChatService,
        private cdr: ChangeDetectorRef,
    ) {}

    ngAfterViewChecked() {
        this.scrollToBottom();
        this.cdr.detectChanges();
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
