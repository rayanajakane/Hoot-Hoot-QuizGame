/* eslint-disable max-lines */
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
//import { HttpResponse } from '@angular/common/http';
import { Component, EventEmitter, Inject, Input, OnChanges, OnInit, Optional, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { QuestionGeneratorComponent } from '@app/components/question-generator/question-generator.component';
import { IMAGE_MAX_FILE_SIZE } from '@app/constants/image-constants';
import { MAX_CHOICES, MIN_CHOICES, SNACK_BAR_DISPLAY_TIME, VALID_MARGIN_FRACTION } from '@app/constants/question-creation';
import { QCM, QRE, QRL } from '@app/constants/question-types';
import { ManagementState } from '@app/constants/states';
import { Choice } from '@app/interfaces/choice';
import { Question } from '@app/interfaces/question';
import { BankService } from '@app/services/bank/bank.service';
import { QuestionService } from '@app/services/question/question.service';
import { QuestionType } from '@common/constants/question-types';
import { translate } from '@jsverse/transloco';

export interface DialogManagement {
    modificationState: ManagementState;
}
@Component({
    selector: 'app-question-creation-form',
    templateUrl: './question-creation-form.component.html',
    styleUrls: ['./question-creation-form.component.scss'],
})
export class QuestionCreationFormComponent implements OnInit, OnChanges {
    @Input() question: Question;
    @Input() modificationState: ManagementState;
    @Output() createQuestionEvent: EventEmitter<Question> = new EventEmitter<Question>();
    @Output() modifyQuestionEvent: EventEmitter<Question> = new EventEmitter<Question>();

    qcm = QCM;
    qre = QRE;
    qrl = QRL;

    modifyingForm: boolean = false;
    questionFormControl = new FormControl('', [Validators.required]);
    questionForm: FormGroup;
    checked: boolean;
    disabled: boolean;
    notificationShown: boolean = false;

    dialogForm: FormGroup;
    loadedImageFile: File | null = null;

    // Allow more constructor parameters to reduce logic in the component
    // eslint-disable-next-line max-params
    constructor(
        private readonly snackBar: MatSnackBar,
        readonly formBuilder: FormBuilder,
        private questionService: QuestionService,
        public bankService: BankService,
        private dialog: MatDialog,

        @Optional() @Inject(MAT_DIALOG_DATA) public dialogData: DialogManagement,
    ) {
        this.initializeForm();
        if (dialogData) {
            this.modificationState = dialogData.modificationState;
        }
    }

    get choices(): FormArray {
        return this.questionForm.get('choices') as FormArray;
    }

    get managementState(): typeof ManagementState {
        return ManagementState;
    }

    handleGeneratedQuestion(generatedQuestion: any) {
        this.questionForm.get('text')?.setValue(generatedQuestion.question);
        this.questionForm.get('type')?.setValue(generatedQuestion.type);

        if (generatedQuestion.type === QuestionType.MultipleChoice) {
            this.questionForm.get('type')?.setValue(QuestionType.MultipleChoice);
            const choicesArray = this.questionForm.get('choices') as FormArray;
            choicesArray?.clear();

            generatedQuestion.choices?.forEach((choice: Choice) => {
                choicesArray.push(
                    this.formBuilder.group({
                        text: choice.text,
                        isCorrect: choice.isCorrect,
                    }),
                );
            });
        }

        if (generatedQuestion.type === QuestionType.EstimatedAnswer) {
            if (this.questionForm.get('type')?.value === QuestionType.EstimatedAnswer) {
                this.questionForm.get('type')?.setValue(QuestionType.EstimatedAnswer);
                const estimatedParams = this.questionForm.get('estimatedParameters') as FormGroup;
                estimatedParams.patchValue({
                    lowerBound: generatedQuestion.lowerBound,
                    upperBound: generatedQuestion.upperBound,
                    correctAnswer: generatedQuestion.exactValue,
                    margin: generatedQuestion.errorMargin,
                });
            }
        }
    }

    openQuestionDialog() {
        const dialogRef = this.dialog.open(QuestionGeneratorComponent, {
            data: {},
        });

        dialogRef.componentInstance.questionGenerated.subscribe((generatedQuestion: any) => {
            this.handleGeneratedQuestion(generatedQuestion);
        });
    }

    closeDialog() {
        this.dialog.closeAll();
    }

    buildChoices(): FormGroup {
        return this.formBuilder.group({
            text: ['', Validators.required],
            isCorrect: [false, Validators.required],
        });
    }

    addChoice() {
        const choices = this.questionForm.get('choices') as FormArray;
        if (choices.length < MAX_CHOICES) {
            this.choices.push(this.buildChoices());
        } else {
            this.openSnackBar(translate('question-creation-form.choices-max-error'), SNACK_BAR_DISPLAY_TIME);
            return;
        }
    }

    dropChoice(event: CdkDragDrop<this>) {
        if (this.questionForm) {
            moveItemInArray(this.choices.controls, event.previousIndex, event.currentIndex);
            this.choices.controls.forEach((control, index) => {
                control.patchValue({ number: index + 1 }, { emitEvent: false });
            });
        }
        if (this.question) {
            this.question.choices = this.questionForm.value.choices;
        }
    }

    submitForm() {
        if (this.questionForm.valid) {
            const newQuestion: Question = this.questionForm.value;
            newQuestion.pictureFile = this.loadedImageFile;
            newQuestion.lastModification = new Date().toLocaleDateString();
            if (this.modificationState === ManagementState.BankModify) {
                this.modifyQuestionEvent.emit(newQuestion);
            } else {
                this.createQuestionEvent.emit(newQuestion);
            }
        }
    }

    removeChoice(index: number) {
        const choices = this.questionForm.get('choices') as FormArray;
        if (choices.length > MIN_CHOICES) {
            this.choices?.removeAt(index);
        } else {
            this.openSnackBar(translate('question-creation-form.choices-min-error'), SNACK_BAR_DISPLAY_TIME);
            return;
        }
    }

    openSnackBar(message: string, duration: number = 0) {
        this.snackBar.open(message, undefined, {
            duration,
        });
    }

    parseGeneratedAnswer(data: { return: string; sessionId: string }) {
        const result = data.return;
        const parsedData = JSON.parse(result);
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
            this.openSnackBar('Erreur lors de la génération de la question', 5000);
            return [];
        }
    }

    ngOnInit(): void {
        if (this.modifyingForm) {
            this.questionForm.valueChanges.subscribe((formValue) => {
                this.question.text = formValue?.text;
                this.question.type = formValue?.type;
                this.question.points = formValue?.points;
                this.question.pictureUrl = formValue?.pictureUrl;
                this.question.pictureFile = formValue?.pictureFile;
                this.question.lastModification = new Date().toLocaleDateString();
                if (this.question.type === QuestionType.MultipleChoice) {
                    this.question.choices = formValue?.choices;
                } else if (this.question.type === QuestionType.EstimatedAnswer) {
                    this.question.estimatedParameters = formValue?.estimatedParameters;
                }
            });
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.question && this.question) {
            this.modifyingForm = true;
            this.updateFormValues();
        }
    }

    getButtonText() {
        switch (this.modificationState) {
            case ManagementState.BankCreate:
                return translate('question-creation-form.add-question-bank');
            case ManagementState.GameCreate:
                return translate('question-creation-form.verify-question');
            case ManagementState.BankModify:
                return translate('question-creation-form.modify-question');
            case ManagementState.GameModify:
                return translate('question-creation-form.modify-question');
        }
    }

    isActiveSubmit() {
        return true;
        // Previously used to forbid changing question type
        // return this.modificationState !== ManagementState.GameModify && this.modificationState !== ManagementState.BankModify;
    }

    toggleBank() {
        this.bankService.addToBank = this.bankService.addToBank ? false : true;
    }

    setPicture(event: Event) {
        const eventTarget: HTMLInputElement | null = event.target as HTMLInputElement | null;
        if (eventTarget?.files?.[0]) {
            const file: File = eventTarget.files[0];
            if (file.size > IMAGE_MAX_FILE_SIZE) {
                this.openSnackBar(translate('question-creation-form.file-big-error'), SNACK_BAR_DISPLAY_TIME);
                return;
            }
            const reader = new FileReader();
            reader.addEventListener('load', () => {
                this.questionForm.get('pictureUrl')?.setValue(reader.result as null);
                this.questionForm.get('pictureFile')?.setValue(file);
                this.loadedImageFile = file;
            });
            reader.readAsDataURL(file);
        }
    }

    removePicture() {
        this.questionForm.get('pictureUrl')?.setValue('');
        this.questionForm.get('pictureFile')?.setValue(null);
        this.loadedImageFile = null;
    }

    private initializeForm(): void {
        this.bankService.addToBank = false;
        this.questionForm = this.formBuilder.group(
            {
                text: ['', Validators.required],
                points: ['', Validators.required],
                type: ['', Validators.required],
                pictureUrl: [''],
                pictureFile: [],
            },
            { validators: this.questionService.validateChoicesLength },
        );

        this.questionForm.statusChanges.subscribe((status) => {
            if (status === 'INVALID') {
                if (this.questionForm.get('text')?.invalid) {
                    return this.showNotification(translate('question-creation-form.required-error'));
                }

                if (this.questionForm.get('choices')?.invalid && this.questionForm.hasError('invalidChoicesLength')) {
                    return this.showNotification(translate('question-creation-form.invalid-choices-length-error'));
                }

                if (this.questionForm.get('estimatedParameters')?.invalid) {
                    const errors = this.questionForm.get('estimatedParameters')?.errors;
                    const errorMessages: { [key: string]: string } = {
                        invalidBounds: translate('question-creation-form.invalid-bounds'),
                        invalidMargin: translate('question-creation-form.invalid-margin'),
                        invalidType: translate('question-creation-form.invalid-type'),
                        negativeMargin: translate('question-creation-form.negative-margin'),
                        correctAnswerOutOfBounds: translate('question-creation-form.correct-answer-out-of-bounds'),
                    };

                    for (const error in errorMessages) {
                        if (errors?.[error]) {
                            return this.showNotification(errorMessages[error]);
                        }
                    }
                }
            }
        });

        this.questionForm.get('type')?.valueChanges.subscribe((type: string) => {
            switch (type) {
                case QuestionType.MultipleChoice: {
                    this.questionForm.addControl(
                        'choices',
                        this.formBuilder.array([
                            this.formBuilder.group({
                                text: ['', Validators.required],
                                isCorrect: [true, Validators.required],
                            }),
                            this.formBuilder.group({
                                text: ['', Validators.required],
                                isCorrect: [false, Validators.required],
                            }),
                        ]),
                    );
                    this.questionForm.removeControl('estimatedParameters');

                    break;
                }
                case QuestionType.LongAnswer: {
                    this.questionForm.removeControl('choices');
                    this.questionForm.removeControl('estimatedParameters');

                    break;
                }
                case QuestionType.EstimatedAnswer: {
                    this.questionForm.removeControl('choices');
                    this.questionForm.addControl(
                        'estimatedParameters',
                        this.formBuilder.group(
                            {
                                lowerBound: [0, [Validators.required, Validators.min(Number.MIN_SAFE_INTEGER)]],
                                upperBound: [1, [Validators.required, Validators.max(Number.MAX_SAFE_INTEGER)]],
                                correctAnswer: [0, Validators.required],
                                margin: [0, [Validators.required, Validators.min(0)]],
                            },
                            {
                                validators: [
                                    (control: AbstractControl) => this.validateEstimationBounds(control as FormGroup),
                                    (control: AbstractControl) => this.validateMargin(control as FormGroup),
                                    (control: AbstractControl) => this.validateParametersType(control as FormGroup),
                                    (control: AbstractControl) => this.validateCorrectAnswer(control as FormGroup),
                                ],
                            },
                        ),
                    );

                    break;
                }
            }
        });
    }

    private validateEstimationBounds(group: FormGroup) {
        const lowerBound = group.get('lowerBound')?.value;
        const upperBound = group.get('upperBound')?.value;
        const correctAnswer = group.get('correctAnswer')?.value;

        if (typeof lowerBound === 'number' && typeof upperBound === 'number' && typeof correctAnswer === 'number') {
            if (lowerBound >= upperBound) {
                return { invalidBounds: true };
            }
            if (correctAnswer < lowerBound || correctAnswer > upperBound) {
                return { correctAnswerOutOfBounds: true };
            }
        }
        return null;
    }

    private validateMargin(group: FormGroup) {
        const margin = group.get('margin')?.value;
        const upperBound = group.get('upperBound')?.value;
        const lowerBound = group.get('lowerBound')?.value;

        if (typeof margin === 'number' && typeof upperBound === 'number' && typeof lowerBound === 'number') {
            if (margin > (upperBound - lowerBound) / VALID_MARGIN_FRACTION) {
                return { invalidMargin: true };
            }
        }

        if (margin < 0) {
            return { negativeMargin: true };
        }
        return null;
    }

    private validateParametersType(group: FormGroup) {
        const fields = ['lowerBound', 'upperBound', 'correctAnswer', 'margin'];
        if (fields.some((field) => group.get(field)?.value === '')) {
            return null;
        }
        if (fields.some((field) => !Number.isInteger(group.get(field)?.value) || group.get(field)?.value.toString().includes('e'))) {
            return { invalidType: true };
        }
        return null;
    }

    private validateCorrectAnswer(group: FormGroup) {
        const lowerBound = group.get('lowerBound')?.value;
        const upperBound = group.get('upperBound')?.value;
        const correctAnswer = group.get('correctAnswer')?.value;

        if (typeof correctAnswer === 'number' && typeof lowerBound === 'number' && typeof upperBound === 'number') {
            if (correctAnswer < lowerBound || correctAnswer > upperBound) {
                return { correctAnswerOutOfBounds: true };
            }
        }
        return null;
    }

    private showNotification(message: string) {
        this.openSnackBar(message, SNACK_BAR_DISPLAY_TIME);
        this.notificationShown = true;
    }

    private updateFormValues(): void {
        this.questionForm.patchValue({
            text: this.question?.text,
            points: this.question?.points,
            type: this.question?.type,
            lastModification: this.question?.lastModification,
            pictureUrl: this.question?.pictureUrl,
        });
        if (this.questionForm.get('type')?.value === QuestionType.MultipleChoice) {
            const choicesArray = this.questionForm.get('choices') as FormArray;
            if (!choicesArray) return;
            choicesArray.clear();
            this.question.choices?.forEach((choice) => {
                if (choice.text) {
                    choicesArray.push(
                        this.formBuilder.group({
                            text: choice.text,
                            isCorrect: choice.isCorrect,
                        }),
                    );
                }
            });
        }
        if (this.questionForm.get('type')?.value === QuestionType.EstimatedAnswer) {
            const estimatedParameters = this.questionForm.get('estimatedParameters') as FormGroup;
            if (!estimatedParameters) return;
            estimatedParameters.patchValue({
                lowerBound: this.question.estimatedParameters?.lowerBound,
                upperBound: this.question.estimatedParameters?.upperBound,
                correctAnswer: this.question.estimatedParameters?.correctAnswer,
                margin: this.question.estimatedParameters?.margin,
            });
        }
    }
}
