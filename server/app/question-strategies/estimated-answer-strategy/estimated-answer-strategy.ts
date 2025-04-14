import { GradingEvents } from '@app/constants/grading-events';
import { PanicThresholdTime } from '@app/constants/panic-threasholds-time';
import { QuestionType } from '@app/constants/question-types';
import { EstimatedAnswer } from '@app/model/answer-types/estimated-choice-answer/estimated-choice-answer';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player } from '@app/model/schema/player.schema';
import { QuestionStrategy } from '@app/question-strategies/question-strategy';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { BONUS_FACTOR } from '@common/constants/match-constants';
import { AnswerEvents } from '@common/events/answer.events';
import { Injectable } from '@nestjs/common';
import { EventEmitter2 } from '@nestjs/event-emitter';

@Injectable()
export class EstimatedAnswerStrategy extends QuestionStrategy {
    constructor(private readonly eventEmitter: EventEmitter2) {
        super(QuestionType.EstimatedAnswer, PanicThresholdTime.EstimatedAnswer);
    }

    gradeAnswers(matchRoom: MatchRoom, players: Player[]): void {
        this.calculateScore(matchRoom, players);
        this.eventEmitter.emit(GradingEvents.GradingComplete, matchRoom.code);
    }

    calculateScore(matchRoom: MatchRoom, players: Player[]) {
        const currentQuestionPoints = matchRoom.currentQuestion.points;
        const margin = matchRoom.currentQuestion.estimatedParameters.margin;
        const correctAnswer: number = parseInt(matchRoom.currentQuestionAnswer[0]);
        players.forEach((player) => {
            if ((player.answer as EstimatedAnswer).answer === Infinity) {
                if (player.answer.timestamp !== Infinity) {
                    const lowerBound = matchRoom.currentQuestion?.estimatedParameters?.lowerBound;
                    (player.answer as EstimatedAnswer).answer = lowerBound;
                }
            }

            console.log('player.answer', player.answer);
            const playerAnswer = (player.answer as EstimatedAnswer).answer;
            if (this.isAnswerWithinMargin(playerAnswer, correctAnswer, margin)) {
                player.answerCorrectness = AnswerCorrectness.GOOD;
                player.nGoodAnswers++;
                player.score += currentQuestionPoints;
                if (this.isCorrectAnswer(playerAnswer, correctAnswer) && margin !== 0) {
                    this.computePlayerBonus(player, currentQuestionPoints);
                }
            }
        });
    }

    private computePlayerBonus(player: Player, currentQuestionPoints: number) {
        const bonus = currentQuestionPoints * BONUS_FACTOR;
        player.score += bonus;
        player.bonusCount++;
        player.socket.emit(AnswerEvents.Bonus, bonus);
    }

    private isCorrectAnswer(playerAnswer: number, correctAnswer: number) {
        return playerAnswer === correctAnswer;
    }

    private isAnswerWithinMargin(playerAnswer: number, correctAnswer: number, margin: number) {
        const gapMargin = Math.abs(playerAnswer - correctAnswer);
        return gapMargin <= margin;
    }
}
