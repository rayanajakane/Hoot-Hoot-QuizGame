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
    currentAnswer: FormControl;
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
        this.showFeedbackSubscription = this.answerService.showingFeedback$.subscribe((showingFeedback) => {
            this.updateInputState(showingFeedback);
        });

        this.updateInputState(this.answerService.showFeedback);
        this.setAnswerforCheater();
    }

    ngOnDestroy() {
        if (this.showFeedbackSubscription) {
            this.showFeedbackSubscription.unsubscribe();
        }
    }

    updateInputState(showFeedback: boolean) {
        this.currentLongAnswerControl[showFeedback ? 'disable' : 'enable']();
    }

    setAnswerforCheater(){
        if (this.matchContextService.getContext() === this.contextOptions.CheaterView) {
           this.currentLongAnswerControl.setValue(this.matchRoomService.currentAnswers[0].toString());
        }
    }

    onInputChange(): void {
        const value = this.currentLongAnswerControl.value;

        if (!/^-?\d+$/.test(value) && value !== '') {
            const cleanedValue = value.replace(/[^\d-]|(?<=.)-/g, '');
            this.currentLongAnswerControl.setValue(cleanedValue);
            return;
        }

        if (value === '' || value === '-') {
            this.resetLongAnswer();
            return;
        }

        this.updateNumericInput(value);
    }

    onSliderChange(event: Event): void {
        const inputElement = event.target as HTMLInputElement;
        const value = inputElement.value;
        this.updateNumericInput(value);
    }

    private updateNumericInput(value: string): void {
        const numValue = Number(value);

        if (numValue >= this.lowerBound && numValue <= this.upperBound) {
            this.isOutOfBounds = false;
            this.setLongAnswer(numValue);
        } else {
            this.isOutOfBounds = true;
            this.resetLongAnswer();
        }
    }

    private resetLongAnswer(): void {
        this.answerService.currentLongAnswer = '';
        this.answerService.updateLongAnswer();
    }

    private setLongAnswer(numValue: number): void {
        this.currentLongAnswerControl.setValue(numValue);
        this.answerService.currentLongAnswer = numValue.toString();
        this.answerService.updateLongAnswer();
    }
}
