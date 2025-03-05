import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, EventEmitter, Inject, Input, OnChanges, OnInit, Optional, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MAX_CHOICES, MIN_CHOICES, SNACK_BAR_DISPLAY_TIME, VALID_MARGIN_FRACTION } from '@app/constants/question-creation';
import { ManagementState } from '@app/constants/states';
import { Question } from '@app/interfaces/question';
import { BankService } from '@app/services/bank/bank.service';
import { QuestionService } from '@app/services/question/question.service';
import { QuestionType } from '@common/constants/question-types';

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

    response: string = '';
    modifyingForm: boolean = false;
    questionFormControl = new FormControl('', [Validators.required]);
    questionForm: FormGroup;
    checked: boolean;
    disabled: boolean;
    notificationShown: boolean = false;

    // Allow more constructor parameters to reduce logic in the component
    // eslint-disable-next-line max-params
    constructor(
        private readonly snackBar: MatSnackBar,
        private readonly formBuilder: FormBuilder,
        private questionService: QuestionService,
        public bankService: BankService,
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
            this.openSnackBar('Il ne peut pas y avoir plus de 4 choix.', SNACK_BAR_DISPLAY_TIME);
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

    onSubmit() {
        if (this.questionForm.valid) {
            const newQuestion: Question = this.questionForm.value;
            newQuestion.lastModification = new Date().toLocaleDateString();
            if (this.modificationState === ManagementState.BankModify) {
                this.modifyQuestionEvent.emit(newQuestion);
            } else {
                this.createQuestionEvent.emit(newQuestion);
            }
            if (this.bankService.addToBank) {
                this.bankService.addQuestion(newQuestion, true);
            }
        }
    }

    removeChoice(index: number) {
        const choices = this.questionForm.get('choices') as FormArray;
        if (choices.length > MIN_CHOICES) {
            this.choices?.removeAt(index);
        } else {
            this.openSnackBar('Il ne peut pas y avoir moins de 2 choix', SNACK_BAR_DISPLAY_TIME);
            return;
        }
    }

    openSnackBar(message: string, duration: number = 0) {
        this.snackBar.open(message, undefined, {
            duration,
        });
    }

    ngOnInit(): void {
        if (this.modifyingForm) {
            this.questionForm.valueChanges.subscribe((formValue) => {
                this.question.text = formValue?.text;
                this.question.type = formValue?.type;
                this.question.points = formValue?.points;
                this.question.lastModification = new Date().toLocaleDateString();
                this.question.choices = formValue?.choices;
                // this.question.estimatedParameters = formValue?.estimatedParameters;
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
                return 'Ajouter la question à la banque';
            case ManagementState.GameCreate:
                return 'Vérifier si la question est valide';
            case ManagementState.BankModify:
                return 'Modifier la question';
            case ManagementState.GameModify:
                return 'Modifier la question';
        }
    }

    isActiveSubmit() {
        return this.modificationState !== ManagementState.GameModify && this.modificationState !== ManagementState.BankModify;
    }

    private initializeForm(): void {
        this.bankService.addToBank = false;
        this.questionForm = this.formBuilder.group(
            {
                text: ['', Validators.required],
                points: ['', Validators.required],
                type: ['', Validators.required],
            },
            { validators: this.questionService.validateChoicesLength },
        );

        this.questionForm.statusChanges.subscribe((status) => {
            if (status === 'INVALID') {
                if (this.questionForm.get('text')?.invalid) {
                    return this.showNotification('Le champ de la question est requis !');
                }

                if (this.questionForm.get('choices')?.invalid && this.questionForm.hasError('invalidChoicesLength')) {
                    return this.showNotification('Il faut au moins une réponse correcte et une incorrecte !');
                }

                if (this.questionForm.get('estimatedParameters')?.invalid) {
                    const errors = this.questionForm.get('estimatedParameters')?.errors;
                    const errorMessages: { [key: string]: string } = {
                        invalidBounds: 'La borne inférieure doit être inférieure à la borne supérieure !',
                        invalidMargin: 'La marge est trop grande ! Elle ne doit représenter que 25% de l`intervalle !',
                        invalidType: 'Les paramètres doivent être des nombres entiers !',
                        negativeMargin: 'La marge ne peut pas être négative !',
                        correctAnswerOutOfBounds: 'La réponse correcte doit être entre les bornes !',
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
        if (fields.some((field) => !Number.isInteger(group.get(field)?.value))) {
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
