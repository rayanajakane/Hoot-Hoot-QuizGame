import { Component } from '@angular/core';
import { MatchContext } from '@app/constants/states';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
@Component({
    selector: 'app-estimated-answer-area',
    templateUrl: './estimated-answer-area.component.html',
    styleUrl: './estimated-answer-area.component.scss',
})
export class EstimatedAnswerAreaComponent {
    constructor(
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        public answerService: AnswerService,
    ) {}

    get contextOptions(): typeof MatchContext {
        return MatchContext;
    }

    get answerOptions(): (string | AnswerCorrectness)[] {
        return Object.values(AnswerCorrectness).filter((value) => isFinite(Number(value)));
    }

    ngOnInit(): void {
        this.answerService.resetStateForNewQuestion();
    }
}
