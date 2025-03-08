import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatchContext } from '@app/constants/states';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
@Component({
    selector: 'app-estimated-answer-area',
    templateUrl: './estimated-answer-area.component.html',
    styleUrl: './estimated-answer-area.component.scss',
})
export class EstimatedAnswerAreaComponent implements OnInit {
    currentLongAnswerControl: FormControl; // ✅ Utilisation de FormControl seul

    constructor(
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        public answerService: AnswerService,
    ) {}

    get contextOptions(): typeof MatchContext {
        return MatchContext;
    }

    ngOnInit(): void {
        const lowerBound = this.matchRoomService.currentQuestion?.estimatedParameters?.lowerBound || 0;
        this.currentLongAnswerControl = new FormControl(lowerBound);
        this.answerService.currentLongAnswer = lowerBound.toString();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    updateAnswerFromSlider(event: any): void {
        if (event && event.target && event.target.value !== undefined) {
            const newValue = Number(event.target.value);
            this.currentLongAnswerControl.setValue(newValue);
            this.answerService.currentLongAnswer = newValue.toString();
            this.answerService.updateLongAnswer();
        }
    }
}
