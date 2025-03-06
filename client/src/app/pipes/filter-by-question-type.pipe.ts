import { Pipe, PipeTransform } from '@angular/core';
import { Question } from '@app/interfaces/question';
import { QuestionType } from '@common/constants/question-types';

@Pipe({
    name: 'filterByQuestionType',
    pure: false,
})
export class FilterByQuestionTypePipe implements PipeTransform {
    transform(questions: Question[], filter: string): Question[] {
        if (Object.values(QuestionType).includes(filter as QuestionType)) {
            return questions.filter((question: Question) => question.type === filter);
        }
        return questions;
    }
}
