import { AfterViewChecked, ChangeDetectorRef, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';

import { Message } from '@common/interfaces/message';

import { ChatService } from '@app/services/chat/chat.service';

@Component({
    selector: 'app-chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements AfterViewChecked, OnInit, OnDestroy {
    @ViewChild('messagesContainer', { static: true }) messagesContainer: ElementRef;

    @Input() disableMessagingField: boolean;

    constructor(
        readonly chatService: ChatService,
        private cdr: ChangeDetectorRef,
    ) {}

    ngOnInit(): void {
        // this.chatService.displayOldMessages();
        this.chatService.handleReceivedMessages();
    }

    ngAfterViewChecked() {
        this.scrollToBottom();
        this.cdr.detectChanges();
    }

    ngOnDestroy() {
        this.chatService.socketHandler.socket.removeListener('newMessage');
        this.chatService.socketHandler.socket.removeListener('fetchOldMessages');
    }

    sendMessage(messageText: string): void {
        const playerUsername = 'TODO';
        if (messageText) {
            const newMessage: Message = {
                text: messageText,
                author: playerUsername,
                date: new Date(),
            };
            this.chatService.sendPrototypeMessage(newMessage);
        }
    }

    private scrollToBottom(): void {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }
}
