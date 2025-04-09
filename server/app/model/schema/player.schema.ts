import { EstimatedAnswer } from '@app/model/answer-types/estimated-choice-answer/estimated-choice-answer';
import { LongAnswer } from '@app/model/answer-types/long-answer/long-answer';
import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { Socket } from 'socket.io';

export interface Player {
    username: string;
    photoUrl: string;
    id: string;
    answer: MultipleChoiceAnswer | LongAnswer | EstimatedAnswer;
    score: number;
    answerCorrectness: AnswerCorrectness;
    bonusCount: number;
    nGoodAnswers: number;
    isPlaying: boolean;
    isChatActive: boolean;
    socket: Socket;
    state: string;
}

export interface VotingData {
    username: string;
    numberOfVotes: number;
    usersWhoVoted:string[];
}
