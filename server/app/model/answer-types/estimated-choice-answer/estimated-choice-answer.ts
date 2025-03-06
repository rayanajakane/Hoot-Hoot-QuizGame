import { Answer } from '@app/model/answer-types/abstract-answer/answer';

export class EstimatedAnswer extends Answer {
    answer: number = Infinity;

    resetAnswer(): void {
        super.resetAnswer();
        this.answer = Infinity;
    }

    updateChoice(_: string, __: boolean, estimation?: number): void {
        if (estimation !== undefined) {
            this.answer = estimation;
        }
    }
}
