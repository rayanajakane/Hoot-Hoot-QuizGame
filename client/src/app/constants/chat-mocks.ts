/* eslint-disable @typescript-eslint/no-magic-numbers */

import { Player } from '@app/interfaces/player';
import { Message } from '@common/interfaces/message';
import { UserIdName } from '@common/interfaces/user-id-name';

export const MOCK_DATE = new Date(2024, 1, 1);

export const MOCK_MESSAGE: Message = {
    id: 'abc',
    text: 'Test Text',
    authorId: 'User',
    authorUsername: 'User',
    photoUrl: 'abc',
    date: MOCK_DATE,
    userLikes: [],
    userLoves: [],
    userDislikes: [],
};

export const MOCK_USER_ID_NAME: UserIdName = {
    id: '1234',
    name: 'mock',
};

export const MOCK_USER_ID_NAME_2: UserIdName = { id: 'abc', name: 'test' };

export const MOCK_MESSAGES: Message[] = [MOCK_MESSAGE];

export const MOCK_ROOM_CODE = '1234';

export const MOCK_USERNAME = MOCK_MESSAGE.authorUsername;

export const PLAYER_MOCK: Player = {
    username: '',
    score: 0,
    bonusCount: 0,
    isPlaying: true,
    isChatActive: true,
    state: '',
};
