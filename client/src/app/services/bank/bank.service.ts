import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Question } from '@app/interfaces/question';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { QuestionService } from '@app/services/question/question.service';
import { translate } from '@jsverse/transloco';

@Injectable({
    providedIn: 'root',
})
export class BankService {
    questions: Question[] = [];
    addToBank: boolean = false;
    isLoadingBank: boolean = false;

    constructor(
        private readonly questionService: QuestionService,
        private readonly notificationService: NotificationService,
        private authenticationService: AuthenticationService,
    ) {}

    getAllQuestions(): void {
        this.isLoadingBank = true;
        this.questionService.getAllQuestions().subscribe({
            next: (data: Question[]) => {
                this.questions = [...data];
                this.isLoadingBank = false;
            },
            error: (error: HttpErrorResponse) =>
                this.notificationService.displayErrorMessage(`${translate('bank-status.unretrieved')}\n ${error.message}`),
        });
    }

    deleteQuestion(questionId: string): void {
        this.questionService.deleteQuestion(questionId).subscribe({
            next: () => {
                this.questions = this.questions.filter((question: Question) => question.id !== questionId);
                this.notificationService.displaySuccessMessage(`${translate('bank-status.deleted')}`);
            },
            error: (error: HttpErrorResponse) => {
                const message = error.message || '';
                const displayMessage = message
                    .split('\n')
                    .map((line) => translate(line.trim()))
                    .join('\n');
                this.notificationService.displayErrorMessage(`${translate('bank-status.still')}\n ${displayMessage}`);
            },
        });
    }

    addQuestion(newQuestion: Question, isModificationPageQuestion: boolean = false): void {
        if (!newQuestion.creatorName) {
            newQuestion.creatorName = this.authenticationService.userDisplayName;
        }
        const pictureFile = newQuestion.pictureFile;
        const isImageToUpload = this.authenticationService.isImageToUpload(newQuestion.pictureUrl);

        if (isImageToUpload) newQuestion.pictureUrl = '';
        newQuestion.pictureFile = null;
        delete newQuestion['pictureFile'];

        this.questionService.createQuestion(newQuestion).subscribe({
            next: async (response: HttpResponse<string>) => {
                if (response.body) {
                    newQuestion = JSON.parse(response.body);
                    if (!pictureFile || !isImageToUpload) {
                        this.addQuestionToLocalBank(newQuestion, isModificationPageQuestion);
                    } else {
                        await this.uploadQuestionPicture(newQuestion, pictureFile, isModificationPageQuestion);
                    }
                }
            },
            error: (error: HttpErrorResponse) =>
                this.notificationService.displayErrorMessage(`${translate('bank-status.failure')}\n ${error.message}`),
        });
    }

    async uploadQuestionPicture(newQuestion: Question, pictureFile: File, isModificationPageQuestion: boolean = false, isNewQuestion = true) {
        const pictureUrl = await this.authenticationService.uploadQuestionPicture(newQuestion.id, pictureFile);
        newQuestion.pictureUrl = pictureUrl;
        this.questionService.updateQuestion(newQuestion).subscribe({
            next: (response: HttpResponse<string>) => {
                if (response.body) {
                    newQuestion = JSON.parse(response.body);
                    if (isNewQuestion) {
                        this.addQuestionToLocalBank(newQuestion, isModificationPageQuestion);
                    } else {
                        const index = this.questions.findIndex((it: Question) => newQuestion.id === it.id);
                        this.questions[index] = newQuestion;
                    }
                }
            },
            error: (error: HttpErrorResponse) =>
                this.notificationService.displayErrorMessage(`${translate('bank-status.failure')}\n ${error.message}`),
        });
    }

    addQuestionToLocalBank(newQuestion: Question, isModificationPageQuestion: boolean) {
        this.questions.push(newQuestion);
        if (isModificationPageQuestion) {
            this.notificationService.displaySuccessMessage(translate('game-status.archived'));
        } else {
            this.notificationService.displaySuccessMessage(translate('bank-status.success'));
        }
    }

    updateQuestion(newQuestion: Question): void {
        if (this.isDuplicateQuestion(newQuestion, this.questions)) {
            this.notificationService.displayErrorMessage(translate('bank-status.duplicate'));
            return;
        }
        const pictureFile = newQuestion.pictureFile;
        const isImageToUpload = this.authenticationService.isImageToUpload(newQuestion.pictureUrl);

        // Reset URL if new image is uploaded
        if (isImageToUpload) newQuestion.pictureUrl = '';

        newQuestion.pictureFile = null;
        delete newQuestion['pictureFile'];

        this.questionService.updateQuestion(newQuestion).subscribe({
            next: async () => {
                if (isImageToUpload && pictureFile) {
                    await this.uploadQuestionPicture(newQuestion, pictureFile, false, false);
                }
                this.notificationService.displaySuccessMessage(translate('bank-status.modified'));
            },
            error: (error: HttpErrorResponse) =>
                this.notificationService.displayErrorMessage(`${translate('bank-status.unmodified')} \n ${error.message}`),
        });
    }

    private isDuplicateQuestion(newQuestion: Question, questionList: Question[]): boolean {
        return !!questionList.find((question) => question.text === newQuestion.text && question.id !== newQuestion.id);
    }
}
