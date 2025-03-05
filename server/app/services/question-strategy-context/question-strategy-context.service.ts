import { QuestionType } from '@app/constants/question-types';
import { EstimatedAnswer } from '@app/model/answer-types/estimated-choice-answer/estimated-choice-answer';
import { LongAnswer } from '@app/model/answer-types/long-answer/long-answer';
import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player } from '@app/model/schema/player.schema';
import { EstimatedAnswerStrategy } from '@app/question-strategies/estimated-answer-strategy/estimated-answer-strategy';
import { LongAnswerStrategy } from '@app/question-strategies/long-answer-strategy/long-answer-strategy';
import { MultipleChoiceStrategy } from '@app/question-strategies/multiple-choice-strategy/multiple-choice-strategy';
import { QuestionStrategy } from '@app/question-strategies/question-strategy';
import { LONG_ANSWER_TIME } from '@common/constants/match-constants';
import { LongAnswerInfo } from '@common/interfaces/long-answer-info';
import { Injectable } from '@nestjs/common';

@Injectable()
export class QuestionStrategyContext {
    private questionStrategies: Map<string, QuestionStrategy>;

    constructor(
        private readonly multipleChoiceStrategy: MultipleChoiceStrategy,
        private readonly longAnswerStrategy: LongAnswerStrategy,
        private readonly estimatedQuestionStrategy: EstimatedAnswerStrategy,
    ) {
        this.questionStrategies = new Map<string, QuestionStrategy>();
    }

    getQuestionStrategy(matchRoomCode: string): string {
        return this.questionStrategies.get(matchRoomCode).type;
    }

    getQuestionPanicThreshold(matchRoomCode: string): number {
        return this.questionStrategies.get(matchRoomCode).panicThresholdTime;
    }

    setQuestionStrategy(matchRoom: MatchRoom) {
        const currentQuestionType = matchRoom.currentQuestion.type;

        switch (currentQuestionType) {
            case QuestionType.MultipleChoice:
                this.setMultipleChoiceStrategy(matchRoom.code);
                matchRoom.players.forEach((player) => (player.answer = new MultipleChoiceAnswer()));
                matchRoom.questionDuration = matchRoom.game.duration;
                break;

            case QuestionType.LongAnswer:
                this.setLongAnswerStrategy(matchRoom.code);
                matchRoom.players.forEach((player) => (player.answer = new LongAnswer()));
                matchRoom.questionDuration = LONG_ANSWER_TIME;
                break;

            case QuestionType.EstimatedAnswer:
                this.setEstimatedAnswerStrategy(matchRoom.code);
                matchRoom.players.forEach((player) => (player.answer = new EstimatedAnswer()));
                matchRoom.questionDuration = matchRoom.game.duration;
                break;
        }
    }

    gradeAnswers(matchRoom: MatchRoom, players: Player[]) {
        this.questionStrategies.get(matchRoom.code).gradeAnswers(matchRoom, players);
    }

    calculateScore(matchRoom: MatchRoom, players: Player[], grades?: LongAnswerInfo[]) {
        this.questionStrategies.get(matchRoom.code).calculateScore(matchRoom, players, grades);
    }

    // buildHistogram(matchRoom: MatchRoom, choice?: string, selection?: boolean): Histogram {
    //     return this.questionStrategies.get(matchRoom.code).buildHistogram(matchRoom, choice, selection);
    // }

    deleteRoom(matchRoomCode: string) {
        this.questionStrategies.delete(matchRoomCode);
    }

    private setMultipleChoiceStrategy(matchRoomCode: string) {
        this.questionStrategies.set(matchRoomCode, this.multipleChoiceStrategy);
    }

    private setLongAnswerStrategy(matchRoomCode: string) {
        this.questionStrategies.set(matchRoomCode, this.longAnswerStrategy);
    }

    private setEstimatedAnswerStrategy(matchRoomCode: string) {
        this.questionStrategies.set(matchRoomCode, this.estimatedQuestionStrategy);
    }
}
