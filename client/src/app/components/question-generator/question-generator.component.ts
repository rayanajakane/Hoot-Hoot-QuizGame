import { Component, EventEmitter, HostListener, Inject, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TextDialogData } from '@app/interfaces/dialog-data/text-dialog-data';
import { QuestionCreationFormComponent } from '../question-creation-form/question-creation-form.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Choice } from '@app/interfaces/choice';
import { HttpResponse } from '@angular/common/http';
import { QuestionService } from '@app/services/question/question.service';

@Component({
    selector: 'app-question-generator',
    templateUrl: './question-generator.component.html',
    styleUrl: './question-generator.component.scss',
})
export class QuestionGeneratorComponent {
    questionCreationComponent: QuestionCreationFormComponent;
    @Output() questionGenerated: EventEmitter<any> = new EventEmitter<any>();
    dialogForm: FormGroup;
    questionText: string;
    choices: Choice[];
    errorMargin: number;
    lowerBound: number;
    upperBound: number;
    exactValue: number;
    fb: FormBuilder;
    numChoices: number = 4;
    parsedAnswer: any;
    constructor(
        private dialogRef: MatDialogRef<unknown>,
        private questionService: QuestionService,
        @Inject(MAT_DIALOG_DATA) public data: TextDialogData,
    ) {}

    @HostListener('window:keyup.Enter', ['$event'])
    onEnterPress(): void {
        this.dialogRef.close(this.data.input);
    }

    parseGeneratedAnswer(data: { return: string; sessionId: string }) {
        const result = data.return;
        const parsedData = JSON.parse(result);
        console.log(parsedData);
        if (parsedData.Question && Array.isArray(parsedData.Choices)) {
            const question = parsedData.Question.trim();

            const choices = parsedData.Choices.map((choice: { isCorrect: boolean; Text: string }) => ({
                text: choice.Text,
                isCorrect: choice.isCorrect,
            }));

            const lowerBound = parsedData.Numericals?.lowerBound;
            const upperBound = parsedData.Numericals?.upperBound;
            const exactValue = parsedData.Numericals?.exactValue;
            const errorMargin = parsedData.Numericals?.errorMargin;

            this.parsedAnswer = {
                question: question,
                choices: choices,
                lowerBound: lowerBound,
                upperBound: upperBound,
                exactValue: exactValue,
                errorMargin: errorMargin,
            };

            return [
                {
                    question: question,
                    choices: choices,
                    lowerBound: lowerBound,
                    upperBound: upperBound,
                    exactValue: exactValue,
                    errorMargin: errorMargin,
                },
            ];
        } else {
            return [];
        }
    }

    generateQuestion() {
        var questionSent = this.data.input;

        if (this.data.type == 'QCM') {
            questionSent = this.data.input + ` avec ${this.numChoices} choix de réponse, une bonne et une mauvaise`;
        }

        if (this.data.type == 'QRE') {
            questionSent = this.data.input + ` avec une valeur exacte et une marge d'erreur et une borne inférieure et supérieure
            +la marge doit représenter que 25% de lintervalle entre les bornes`;
        }

        this.questionService.generateQuestion(questionSent).subscribe((response: HttpResponse<string>) => {
            if (response.body) {
                const generatedQuestion = JSON.parse(response.body);
                const parsedAnswer = this.parseGeneratedAnswer(generatedQuestion);

                this.questionText = parsedAnswer[0].question;

                if (this.data.type === 'QCM') {
                    this.choices = [];
                    parsedAnswer[0].choices.forEach((choice: Choice, index: number) => {
                        this.choices.push({
                            text: choice.text,
                            isCorrect: choice.isCorrect,
                        });
                    });
                }

                if (this.data.type === 'QRE') {
                    this.lowerBound = parsedAnswer[0].lowerBound;
                    this.upperBound = parsedAnswer[0].upperBound;
                    this.exactValue = parsedAnswer[0].exactValue;
                   // this.errorMargin = parsedAnswer[0].errorMargin;
                    const marginValue = (parsedAnswer[0].upperBound - parsedAnswer[0].lowerBound) * 0.25;
                    console.log(marginValue);
                    if (parsedAnswer[0].errorMargin >= marginValue) {
                        const correctMargin = Math.floor(Math.random() * marginValue);
                        this.errorMargin = correctMargin;
                        parsedAnswer[0].errorMargin = correctMargin;
                        console.log(correctMargin);
                    } else this.errorMargin = parsedAnswer[0].errorMargin;
                }
                const questionData = {
                    question: parsedAnswer[0],
                    type: this.data.type,
                };
                return questionData;
            }
            return null;
        });
    }

    generateChoiceFields() {
        this.choices = [];
        for (let i = 0; i < this.numChoices; i++) {
            // this.choices.push({ text: '' });
        }
    }

    submitDialog() {
        if (this.dialogForm.valid) {
            this.dialogRef.close(this.dialogForm.value.text);
        }
    }

    submitToQuestionForm() {
        if (this.parsedAnswer) {
            const questionData = {
                question: this.parsedAnswer.question,
                choices: this.parsedAnswer.choices,
                lowerBound: this.parsedAnswer.lowerBound,
                upperBound: this.parsedAnswer.upperBound,
                exactValue: this.exactValue,
                errorMargin: this.errorMargin,
                type: this.data.type,
            };

            this.questionGenerated.emit(questionData);
        }
    }

    closeDialog() {
        this.dialogRef.close();
    }
}
