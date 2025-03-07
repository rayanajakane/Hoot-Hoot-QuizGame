import { ChatEmoji } from '../constants/chat-emojis';
import { Message } from './message';
import { UserIdName } from './user-id-name';

export interface MessageInfo {
    roomCode: string;
    message: Message;
}

export interface MessageEmojiInfo {
    messageId: string;
    chatEmoji: ChatEmoji;
    userIdName: UserIdName;
    roomCode?: string;
}

export interface ChatStateInfo {
    roomCode: string;
    playerUsername: string;
}
