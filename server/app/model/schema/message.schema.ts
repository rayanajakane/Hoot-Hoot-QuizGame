import { UserIdName } from '@common/interfaces/user-id-name';

export interface Message {
    id: string;
    text: string;
    authorId: string;
    authorUsername: string;
    photoUrl: string;
    date: Date;
    userLikes: UserIdName[];
    userLoves: UserIdName[];
    userDislikes: UserIdName[];
}
