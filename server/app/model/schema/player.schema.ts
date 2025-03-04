import { LongAnswer } from '@app/model/answer-types/long-answer/long-answer';
import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { Socket } from 'socket.io';
import { EstimatedAnswer } from '../answer-types/estimated-answer/estimated-choice-answer';

export interface Player {
    username: string;
    answer: MultipleChoiceAnswer | LongAnswer | EstimatedAnswer;
    score: number;
    answerCorrectness: AnswerCorrectness;
    bonusCount: number;
    isPlaying: boolean;
    isChatActive: boolean;
    socket: Socket;
    state: string;
}
