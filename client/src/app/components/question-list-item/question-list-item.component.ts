import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ManagementState } from '@app/constants/states';
import { Question } from '@app/interfaces/question';

@Component({
    selector: 'app-question-list-item',
    templateUrl: './question-list-item.component.html',
    styleUrls: ['./question-list-item.component.scss'],
})
export class QuestionListItemComponent implements OnInit {
    @Input() question: Question;
    @Input() index: number;
    @Input() isBankQuestion: boolean;
    @Input() isExpanded: boolean;
    @Output() deleteQuestionEvent = new EventEmitter<string>();
    @Output() updateQuestionEvent = new EventEmitter<Question>();

    questions: Question[] = [];
    modificationState: ManagementState;

    ngOnInit() {
        this.modificationState = this.isBankQuestion ? ManagementState.BankModify : ManagementState.GameModify;
    }

    deleteQuestion() {
        if (!this.question.id) {
            this.question.id = 'questionToDelete';
        }
        this.deleteQuestionEvent.emit(this.question.id);
    }

    dispatchModifiedQuestion() {
        this.updateQuestionEvent.emit(this.question);
    }
}
