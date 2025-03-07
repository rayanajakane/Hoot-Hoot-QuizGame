import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BankStatus, GameStatus } from '@app/constants/feedback-messages';
import { Question } from '@app/interfaces/question';
import { NotificationService } from '@app/services/notification/notification.service';
import { QuestionService } from '@app/services/question/question.service';
import { AuthenticationService } from '../authentication/authentication.service';

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
            error: (error: HttpErrorResponse) => this.notificationService.displayErrorMessage(`${BankStatus.UNRETRIEVED}\n ${error.message}`),
        });
    }

    deleteQuestion(questionId: string): void {
        this.questionService.deleteQuestion(questionId).subscribe({
            next: () => {
                this.authenticationService.deleteBankQuestionPicture(questionId); // TODO: Check case where bank question has no image on Firebase storage
                this.questions = this.questions.filter((question: Question) => question.id !== questionId);
                this.notificationService.displaySuccessMessage(`${BankStatus.DELETED}`);
            },
            error: (error: HttpErrorResponse) => this.notificationService.displayErrorMessage(`${BankStatus.STILL}\n ${error.message}`),
        });
    }

    async addQuestion(newQuestion: Question, isModificationPageQuestion: boolean = false) {
        console.log('Bank add question');
        console.log(newQuestion);

        if (!newQuestion.pictureFile) {
            newQuestion.pictureFile = await this.authenticationService.urlToObject(newQuestion.pictureUrl);
            console.log('After pictureFile edit');
            console.log(newQuestion);
        }

        const pictureFile = newQuestion.pictureFile;
        const isImageToUpload = this.isImageToUploadToBank(newQuestion.pictureUrl);

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
            error: (error: HttpErrorResponse) => this.notificationService.displayErrorMessage(`${BankStatus.FAILURE}\n ${error.message}`),
        });
    }

    async uploadQuestionPicture(newQuestion: Question, pictureFile: File, isModificationPageQuestion: boolean = false, isNewQuestion = true) {
        const pictureUrl = await this.authenticationService.uploadBankQuestionPicture(newQuestion.id, pictureFile);
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
            error: (error: HttpErrorResponse) => this.notificationService.displayErrorMessage(`${BankStatus.FAILURE}\n ${error.message}`),
        });
    }

    addQuestionToLocalBank(newQuestion: Question, isModificationPageQuestion: boolean) {
        this.questions.push(newQuestion);
        if (isModificationPageQuestion) {
            this.notificationService.displaySuccessMessage(GameStatus.ARCHIVED);
        } else {
            this.notificationService.displaySuccessMessage(BankStatus.SUCCESS);
        }
    }

    isImageToUploadToBank(pictureUrl: string) {
        return pictureUrl !== '' && !pictureUrl.includes('bankQuestionPictures');
    }

    updateQuestion(newQuestion: Question) {
        if (this.isDuplicateQuestion(newQuestion, this.questions)) {
            this.notificationService.displayErrorMessage(BankStatus.DUPLICATE);
            return;
        }

        const pictureFile = newQuestion.pictureFile;
        const isImageToUpload = this.isImageToUploadToBank(newQuestion.pictureUrl);

        // Reset URL if new image is uploaded
        if (isImageToUpload) newQuestion.pictureUrl = '';

        newQuestion.pictureFile = null;
        delete newQuestion['pictureFile'];

        this.questionService.updateQuestion(newQuestion).subscribe({
            next: async () => {
                if (isImageToUpload && pictureFile) {
                    await this.uploadQuestionPicture(newQuestion, pictureFile, false, false);
                    console.log(newQuestion);
                }
                this.notificationService.displaySuccessMessage(BankStatus.MODIFIED);
            },
            error: (error: HttpErrorResponse) => this.notificationService.displayErrorMessage(`${BankStatus.UNMODIFIED} \n ${error.message}`),
        });
    }

    private isDuplicateQuestion(newQuestion: Question, questionList: Question[]): boolean {
        return !!questionList.find((question) => question.text === newQuestion.text && question.id !== newQuestion.id);
    }
}
