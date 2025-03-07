import { Message } from '@app/model/schema/message.schema';
import { ChatEmoji } from '@common/constants/chat-emojis';
import { ChatStateInfo, MessageEmojiInfo } from '@common/interfaces/message-info';

export const MOCK_MESSAGE: Message = {
    id: 'abc',
    text: 'Test Text',
    authorId: 'User',
    authorUsername: 'User',
    photoUrl: 'abc',
    date: new Date(),
    userLikes: [],
    userLoves: [],
    userDislikes: [],
};
export const MOCK_ROOM_CODE = '1234';

export const MOCK_USER_ID_NAME = {
    id: '1234',
    name: 'mock',
};

export const MOCK_MESSAGE_EMOJI_INFO: MessageEmojiInfo = {
    messageId: 'abc1234',
    chatEmoji: ChatEmoji.LIKE,
    userIdName: { id: '1234', name: 'mock' },
    roomCode: '7777',
};

export const MOCK_CHAT_STATE_DATA: ChatStateInfo = {
    roomCode: MOCK_ROOM_CODE,
    playerUsername: '',
};

export const MOCK_MATCH_ROOM_INDEX = 0;
export const MOCK_PLAYER_INDEX = 0;

export const MOCK_YEAR = 2024;
export const MOCK_DATE = new Date(MOCK_YEAR, 1, 1);
