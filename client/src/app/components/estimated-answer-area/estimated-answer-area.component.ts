import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatchContext } from '@app/constants/states';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-estimated-answer-area',
    templateUrl: './estimated-answer-area.component.html',
    styleUrl: './estimated-answer-area.component.scss',
})
export class EstimatedAnswerAreaComponent implements OnInit, OnDestroy {
    currentLongAnswerControl: FormControl;
    lowerBound: number;
    upperBound: number;
    isOutOfBounds: boolean = false;
    private showFeedbackSubscription: Subscription;

    constructor(
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        public answerService: AnswerService,
    ) {}

    get contextOptions(): typeof MatchContext {
        return MatchContext;
    }

    ngOnInit(): void {
        const estimatedParams = this.matchRoomService.currentQuestion?.estimatedParameters;
        this.lowerBound = estimatedParams?.lowerBound ?? Number.MIN_SAFE_INTEGER;
        this.upperBound = estimatedParams?.upperBound ?? Number.MAX_SAFE_INTEGER;
        this.currentLongAnswerControl = new FormControl(this.answerService.currentLongAnswer || this.lowerBound.toString());
        this.showFeedbackSubscription = this.answerService.showFeedback$.subscribe((showFeedback) => {
            this.updateInputState(showFeedback);
        });

        this.updateInputState(this.answerService.showFeedback);
    }

    ngOnDestroy() {
        if (this.showFeedbackSubscription) {
            this.showFeedbackSubscription.unsubscribe();
        }
    }

    updateInputState(showFeedback: boolean) {
        this.currentLongAnswerControl[showFeedback ? 'disable' : 'enable']();
    }

    onInputChange(): void {
        const value = this.currentLongAnswerControl.value;

        if (!/^-?\d+$/.test(value) && value !== '') {
            const cleanedValue = value.replace(/[^\d-]|(?<=.)-/g, '');
            this.currentLongAnswerControl.setValue(cleanedValue);
            return;
        }

        if (value === '' || value === '-') {
            this.emptyLongAnswer();
            return;
        }

        this.updateNumericInput(value);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onSliderChange(event: any): void {
        this.updateNumericInput(event.target.value);
    }

    private updateNumericInput(value: string): void {
        const numValue = Number(value);

        if (numValue >= this.lowerBound && numValue <= this.upperBound) {
            this.isOutOfBounds = false;
            this.setLongAnswer(numValue);
        } else {
            this.isOutOfBounds = true;
            this.emptyLongAnswer();
        }
    }

    private emptyLongAnswer(): void {
        this.answerService.currentLongAnswer = '';
        this.answerService.updateLongAnswer();
    }

    private setLongAnswer(numValue: number): void {
        this.currentLongAnswerControl.setValue(numValue);
        this.answerService.currentLongAnswer = numValue.toString();
        this.answerService.updateLongAnswer();
    }
}
