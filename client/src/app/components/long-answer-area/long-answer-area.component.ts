import { Component, OnInit } from '@angular/core';
import { AnswerService } from '@app/services/answer/answer.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { FREE_ANSWER_MAX_LENGTH } from '@common/constants/match-constants';

@Component({
    selector: 'app-long-answer-area',
    templateUrl: './long-answer-area.component.html',
    styleUrls: ['./long-answer-area.component.scss'],
})
export class LongAnswerAreaComponent implements OnInit {
    answerMaxLength = FREE_ANSWER_MAX_LENGTH;

    constructor(
        public matchRoomService: MatchRoomService,
        public matchContextService: MatchContextService,
        public answerService: AnswerService,
    ) {}

    get answerOptions(): (string | AnswerCorrectness)[] {
        return Object.values(AnswerCorrectness).filter((value) => isFinite(Number(value)));
    }

    ngOnInit(): void {
        this.answerService.resetStateForNewQuestion();
    }
}
