import { Component, EventEmitter, HostListener, Inject, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TextDialogData } from '@app/interfaces/dialog-data/text-dialog-data';
import { QuestionCreationFormComponent } from '../question-creation-form/question-creation-form.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Choice } from '@app/interfaces/choice';
import { HttpResponse } from '@angular/common/http';
import { QuestionService } from '@app/services/question/question.service';
//import { TranslocoService } from '@jsverse/transloco';

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
    questionText1: string;
    generatedQuestions: GeneratedQuestion[] = [];
    generateButton: boolean = true;
    answerGenerated: boolean = false;
    choices: Choice[];
    choices1: Choice[];
  
    errorMargin: number;
    lowerBound: number;
    upperBound: number;
    exactValue: number;
    errorMargin1: number;
    lowerBound1: number;
    upperBound1: number;
    exactValue1: number;
    questionSubmitted: boolean = false;
    fb: FormBuilder;
    numChoices: number = 4;
    selectedQuestion: GeneratedQuestion | null = null;
    QuestionFormGenerated: number = 0;
    index: number = 0;
    parsedAnswer1: any;
    parsedAnswer2: any;
    language: string = "Français";
    languageToggle: boolean = false;
    results: [];
    constructor(
        private dialogRef: MatDialogRef<unknown>,
        private questionService: QuestionService,
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
        this.setVAluesToForms(parsedData);
        this.answerGenerated = true;
    }

    onLanguageChange() {
        this.language = this.languageToggle ?  'English': 'Français' ;
      }

    setVAluesToForms(parsedData: any) {
        this.answerGenerated = true;
        console.log(parsedData.Questions[this.index].Question)
        console.log(parsedData.Questions)
        console.log(this.index)
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

            this.parsedAnswer1 = {
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
            console.log('6', this.index);

            console.log('parsedAnswe1', this.parsedAnswer2);
        }
        if (this.index <= 2) {
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

            this.parsedAnswer2 = {
                question: question,
                choices: choices,
                lowerBound: lowerBound,
                upperBound: upperBound,
                exactValue: exactValue,
                errorMargin: errorMargin,
            };



            this.questionText1 = question;
            this.choices1 = choices;
            this.lowerBound1 = lowerBound;
            this.upperBound1 = upperBound;
            this.exactValue1 = exactValue;
            this.errorMargin1 = errorMargin;

        }
    }

    selectQuestion(question: GeneratedQuestion) {
        this.selectedQuestion = question;
    }

    regenerateQuestion() {
        this.answerGenerated = false;
        this.generateButton = false;
        if (this.index <= 2) {
            this.index++;
            this.setVAluesToForms(this.results);
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
        if(this.language === 'Français'){
            var questionSent = this.data.input + ' regénérer des questions DIFFERENTES en FRANÇAIS';
        }
        if(this.language === 'English'){
            var questionSent = this.data.input + ' regenerate DIFFERENT questions in ENGLISH';
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
        console.log(lowerBound, exactValue);
        if (exactValue <= lowerBound) {
            const newLowerBound = exactValue - Math.floor(Math.random() * exactValue);
            return newLowerBound;
        } else return lowerBound;
    }

    validateQREHigherBound(upperBound: number, exactValue: number) {
        console.log(upperBound, exactValue);
        if (exactValue >= upperBound) {
            const newUpperBound = exactValue + Math.floor(Math.random() * (exactValue + upperBound));


            return newUpperBound;
        } else return upperBound;
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
        if (this.parsedAnswer1) {
            const questionData = {
                question: this.parsedAnswer1.question,
                choices: this.parsedAnswer1.choices,
                lowerBound: this.parsedAnswer1.lowerBound,
                upperBound: this.parsedAnswer1.upperBound,
                exactValue: this.parsedAnswer1.exactValue,
                errorMargin: this.parsedAnswer1.errorMargin,
                type: this.data.type,
            };

            this.questionGenerated.emit(questionData);
        }
    }

    submitToQuestion2Form() {
        if (this.parsedAnswer1) {
            const questionData = {
                question: this.parsedAnswer2.question,
                choices: this.parsedAnswer2.choices,
                lowerBound: this.parsedAnswer2.lowerBound,
                upperBound: this.parsedAnswer2.upperBound,
                exactValue: this.parsedAnswer2.exactValue,
                errorMargin: this.parsedAnswer2.errorMargin,
                type: this.data.type,
            };

            this.questionGenerated.emit(questionData);
        }
    }

    closeDialog() {
        this.dialogRef.close();
    }
}
