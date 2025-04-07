import { Component, EventEmitter, HostListener, Inject, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TextDialogData } from '@app/interfaces/dialog-data/text-dialog-data';
import { QuestionCreationFormComponent } from '../question-creation-form/question-creation-form.component';
import { FormGroup } from '@angular/forms';
import { Choice } from '@app/interfaces/choice';
import { HttpResponse } from '@angular/common/http';
import { QuestionService } from '@app/services/question/question.service';
import { TranslocoService } from '@jsverse/transloco';
import { translate } from '@jsverse/transloco';

interface GeneratedQuestion {
    question: {
        question: string;
        choices?: Choice[];
        lowerBound?: number;
        upperBound?: number;
        exactValue?: number;
        errorMargin?: number;
    };
    type: string;
}

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
    questionTextSecondForm: string;
    generatedQuestions: GeneratedQuestion[] = [];
    generateButton: boolean = true;
    answerGenerated: boolean = false;
    choices: Choice[];
    choicesSecondForm: Choice[];
    allResults: [];
    errorMargin: number;
    lowerBound: number;
    upperBound: number;
    exactValue: number;
    errorMarginSecondForm: number;
    lowerBoundSecondForm: number;
    upperBoundSecondForm: number;
    exactValueSecondForm: number;
    questionSubmitted: boolean = false;
    numChoices: number = 4;
    selectedQuestion: GeneratedQuestion | null = null;
    QuestionFormGenerated: number = 0;
    index: number = 0;
    parsedAnswerFirstForm: any;
    parsedAnswerSecondForm: any;
    language: string =
        this.transloco.getActiveLang() === 'fr'
            ? translate('questions-generations.french')
            : this.transloco.getActiveLang() === 'en'
            ? translate('questions-generations.english')
            : this.transloco.getActiveLang();
    results: [];
    constructor(
        private dialogRef: MatDialogRef<unknown>,
        private questionService: QuestionService,
        private transloco: TranslocoService,
        @Inject(MAT_DIALOG_DATA) public data: TextDialogData,
    ) {}

    @HostListener('window:keyup.Enter', ['$event'])
    onEnterPress(): void {
        this.dialogRef.close(this.data.input);
    }

    ngOnInit() {
        this.index = 0;
    }

    parseGeneratedAnswer(data: { return: string; sessionId: string }) {
        const result = data.return;
        const parsedData = JSON.parse(result);
        this.results = parsedData;
        this.setValuesToForms(parsedData);
        this.answerGenerated = true;
    }

    setValuesToForms(parsedData: any) {
        this.answerGenerated = true;
        if (parsedData.Questions[this.index].Question || Array.isArray(parsedData.Questions[this.index].Choices)) {
            const question = parsedData.Questions[this.index].Question.trim();

            const choices = parsedData.Questions[this.index]?.Choices?.map((choice: { isCorrect: boolean; Text: string }) => ({
                text: choice.Text,
                isCorrect: choice.isCorrect,
            }));

            const exactValue = parsedData.Questions[this.index].Numericals?.exactValue;

            const lowerBound = this.validateQRELowerBound(
                parsedData.Questions[this.index].Numericals?.lowerBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
            );
            const upperBound = this.validateQREHigherBound(
                parsedData.Questions[this.index].Numericals?.upperBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
            );
            const errorMargin = this.validateQREAnswerMargin(
                parsedData.Questions[this.index].Numericals?.lowerBound,
                parsedData.Questions[this.index].Numericals?.upperBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
                parsedData.Questions[this.index].Numericals?.errorMargin,
            );

            this.parsedAnswerFirstForm = {
                question: question,
                choices: choices,
                lowerBound: lowerBound,
                upperBound: upperBound,
                exactValue: exactValue,
                errorMargin: errorMargin,
            };

            this.questionText = question;
            this.choices = choices;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.exactValue = exactValue;
            this.errorMargin = errorMargin;
        }
        if (this.index <= 6) {
            this.index++;
        } else this.index = 0;

        if (parsedData.Questions[this.index].Question || Array.isArray(parsedData.Questions[this.index].Choices)) {
            const question = parsedData.Questions[this.index].Question.trim();

            const choices = parsedData.Questions[this.index]?.Choices?.map((choice: { isCorrect: boolean; Text: string }) => ({
                text: choice.Text,
                isCorrect: choice.isCorrect,
            }));

            const exactValue = parsedData.Questions[this.index].Numericals?.exactValue;
            const lowerBound = this.validateQRELowerBound(
                parsedData.Questions[this.index].Numericals?.lowerBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
            );
            const upperBound = this.validateQREHigherBound(
                parsedData.Questions[this.index].Numericals?.upperBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
            );
            const errorMargin = this.validateQREAnswerMargin(
                parsedData.Questions[this.index].Numericals?.lowerBound,
                parsedData.Questions[this.index].Numericals?.upperBound,
                parsedData.Questions[this.index].Numericals?.exactValue,
                parsedData.Questions[this.index].Numericals?.errorMargin,
            );

            this.parsedAnswerSecondForm = {
                question: question,
                choices: choices,
                lowerBound: lowerBound,
                upperBound: upperBound,
                exactValue: exactValue,
                errorMargin: errorMargin,
            };

            this.questionTextSecondForm = question;
            this.choicesSecondForm = choices;
            this.lowerBoundSecondForm = lowerBound;
            this.upperBoundSecondForm = upperBound;
            this.exactValueSecondForm = exactValue;
            this.errorMarginSecondForm = errorMargin;
        }
    }

    selectQuestion(question: GeneratedQuestion) {
        this.selectedQuestion = question;
    }

    regenerateQuestion() {
        this.answerGenerated = false;
        this.generateButton = false;
        if (this.index <= 6) {
            this.index++;
            this.setValuesToForms(this.results);
        } else {
            this.index = 0;
            this.generateQuestion();
        }
    }

    generateQuestion() {
        this.questionSubmitted = true;
        this.generateButton = false;
        this.QuestionFormGenerated++;
        var questionSent = this.data.input;
        this.generatedQuestions = [];
        if (this.language === translate('questions-generations.french')) {
            var questionSent = this.data.input + ' regénérer des questions DIFFERENTES en FRANÇAIS, differentes de celles déjà générées';
        }
        if (this.language === translate('questions-generations.english')) {
            var questionSent = this.data.input + ' regenerate DIFFERENT questions in ENGLISH, diferent from those already generated';
        }

        if (this.data.type == 'QCM') {
            questionSent = questionSent + ` avec ${this.numChoices} choix de réponse, une bonne et une mauvaise`;
        }

        if (this.data.type == 'QRE') {
            questionSent =
                questionSent +
                `(without mentioning it in the question phrasing) avec une valeur exacte et une marge d'erreur et une borne inférieure et supérieure
            +la marge doit représenter que 25% de lintervalle entre les bornes`;
        }

        this.questionService.generateQuestion(questionSent).subscribe((response: HttpResponse<string>) => {
            if (response.body) {
                const generatedQuestion = JSON.parse(response.body);
                this.parseGeneratedAnswer(generatedQuestion);
            }
            return null;
        });
    }

    validateQREAnswerMargin(lowerBound: number, upperBound: number, exactValue: number, errorMargin: number) {
        const marginValue = (upperBound - lowerBound) * 0.25;
        if (errorMargin >= marginValue) {
            const correctMargin = Math.floor(Math.random() * marginValue);
            errorMargin = correctMargin;
            return correctMargin;
        } else return errorMargin;
    }

    validateQRELowerBound(lowerBound: number, exactValue: number) {
        if (exactValue <= lowerBound) {
            if (exactValue === 0) {
                return -Math.round(Math.random() * 100);
            }
            const newLowerBound = exactValue - Math.floor(Math.random() * exactValue);
            return Math.round(newLowerBound);
        } else return Math.round(lowerBound);
    }

    validateQREHigherBound(upperBound: number, exactValue: number) {
        if (upperBound === 0) {
            return Math.round(Math.random() + 1);
        }
        if (exactValue >= upperBound) {
            const newUpperBound = exactValue + Math.floor(Math.random() * (exactValue + upperBound));

            return Math.round(newUpperBound);
        } else return Math.round(upperBound);
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

    submitToQuestion1Form() {
        if (this.parsedAnswerFirstForm) {
            const questionData = {
                question: this.parsedAnswerFirstForm.question,
                choices: this.parsedAnswerFirstForm.choices,
                lowerBound: this.parsedAnswerFirstForm.lowerBound,
                upperBound: this.parsedAnswerFirstForm.upperBound,
                exactValue: this.parsedAnswerFirstForm.exactValue,
                errorMargin: this.parsedAnswerFirstForm.errorMargin,
                type: this.data.type,
            };

            this.questionGenerated.emit(questionData);
        }
    }

    submitToQuestion2Form() {
        if (this.parsedAnswerFirstForm) {
            const questionData = {
                question: this.parsedAnswerSecondForm.question,
                choices: this.parsedAnswerSecondForm.choices,
                lowerBound: this.parsedAnswerSecondForm.lowerBound,
                upperBound: this.parsedAnswerSecondForm.upperBound,
                exactValue: this.parsedAnswerSecondForm.exactValue,
                errorMargin: this.parsedAnswerSecondForm.errorMargin,
                type: this.data.type,
            };

            this.questionGenerated.emit(questionData);
        }
    }

    closeDialog() {
        this.dialogRef.close();
    }
}
