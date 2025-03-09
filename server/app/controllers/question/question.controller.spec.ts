/* eslint-disable @typescript-eslint/no-explicit-any */
import { getMockQuestion } from '@app/constants/question-mocks';
import { ERROR_QUESTION_NOT_FOUND } from '@app/constants/request-errors';
import { Question } from '@app/model/database/question';
import { QuestionPicturesDeletionService } from '@app/services/question-pictures-deletion/question-pictures-deletion.service';
import { QuestionService } from '@app/services/question/question.service';
import { HttpStatus } from '@nestjs/common';
import { Test, TestingModule } from '@nestjs/testing';
import { Response } from 'express';
import { SinonStubbedInstance, createStubInstance } from 'sinon';
import { QuestionController } from './question.controller';

describe('QuestionController', () => {
    let controller: QuestionController;
    let questionService: SinonStubbedInstance<QuestionService>;
    let deletionService: SinonStubbedInstance<QuestionPicturesDeletionService>;

    beforeEach(async () => {
        questionService = createStubInstance(QuestionService);
        deletionService = createStubInstance(QuestionPicturesDeletionService);
        const module: TestingModule = await Test.createTestingModule({
            controllers: [QuestionController],
            providers: [
                {
                    provide: QuestionService,
                    useValue: questionService,
                },
                {
                    provide: QuestionPicturesDeletionService,
                    useValue: deletionService,
                },
            ],
        }).compile();

        controller = module.get<QuestionController>(QuestionController);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });

    it('getAllQuestions() should return all questions', async () => {
        const fakeQuestions = [new Question(), new Question()];
        questionService.getAllQuestions.resolves(fakeQuestions);
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.OK);
            return res;
        };
        res.json = (questions) => {
            expect(questions).toEqual(fakeQuestions);
            return res;
        };

        await controller.getAllQuestions(res);
    });

    it('getAllQuestions() should return NOT FOUND if the service fails', async () => {
        questionService.getAllQuestions.rejects('');
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.NOT_FOUND);
            return res;
        };
        res.send = () => res;
        await controller.getAllQuestions(res);
    });

    it('getQuestionById() should return the question with the corresponding ID', async () => {
        const fakeQuestion = new Question();
        questionService.getQuestionById.resolves(fakeQuestion);

        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.OK);
            return res;
        };
        res.json = (questions) => {
            expect(questions).toEqual(fakeQuestion);
            return res;
        };

        await controller.getQuestionById('', res);
    });

    it('getQuestionById() should return NOT_FOUND when service is unable to fetch the question', async () => {
        questionService.getQuestionById.rejects();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.NOT_FOUND);
            return res;
        };
        res.send = () => res;

        await controller.getQuestionById('', res);
    });

    it('addQuestion() should succeed if service is able to add the question', async () => {
        questionService.addQuestion.resolves();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.CREATED);
            return res;
        };
        res.send = () => res;
        await controller.addQuestion(new Question(), res);
    });

    it('addQuestion() should return BAD_REQUEST when service is not able to find the course', async () => {
        questionService.addQuestion.rejects();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.BAD_REQUEST);
            return res;
        };
        res.send = () => res;
        await controller.addQuestion(new Question(), res);
    });

    it('updateQuestion() should succeed if service is able to update the question', async () => {
        const mockQuestion: Question = getMockQuestion();
        mockQuestion.pictureUrl = 'mock';
        jest.spyOn(questionService, 'getQuestionById').mockResolvedValue(mockQuestion);
        jest.spyOn(questionService, 'updateQuestion').mockResolvedValue(mockQuestion);
        jest.spyOn(deletionService, 'deleteQuestionNonUsedPicture').mockResolvedValue();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.OK);
            return res;
        };
        res.json = (question) => {
            return res;
        };
        res.send = () => res;
        await controller.updateQuestion(mockQuestion, res);
    });

    it('updateQuestion() should return NOT_FOUND when service cannot find the question', async () => {
        jest.spyOn(questionService, 'updateQuestion').mockImplementationOnce(async () => {
            return Promise.reject(ERROR_QUESTION_NOT_FOUND);
        });
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.NOT_FOUND);
            return res;
        };

        res.send = () => res;
        await controller.updateQuestion(new Question(), res);
    });

    it('updateQuestion() should return BAD_REQUEST when service cannot update the question', async () => {
        questionService.updateQuestion.rejects();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.BAD_REQUEST);
            return res;
        };
        res.send = () => res;
        await controller.updateQuestion(new Question(), res);
    });

    it('deleteQuestion() should succeed if service is able to delete the question', async () => {
        questionService.getQuestionById.resolves(getMockQuestion());
        questionService.deleteQuestion.resolves(getMockQuestion());
        deletionService.deleteQuestionNonUsedPicture.resolves();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.NO_CONTENT);
            return res;
        };
        res.send = () => res;
        await controller.deleteQuestion('', res);
    });

    it('deleteQuestion() should return NOT_FOUND when service cannot delete the question', async () => {
        questionService.deleteQuestion.rejects();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.NOT_FOUND);
            return res;
        };
        res.send = () => res;
        await controller.deleteQuestion('', res);
    });

    it('validateQuestion() should return OK if the question is valid.', async () => {
        questionService.validateNewQuestion.resolves();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.OK);
            return res;
        };
        res.send = () => res;
        await controller.validateQuestion(new Question(), res);
    });

    it('validateQuestion() should return BAD_REQUEST if the question is invalid.', async () => {
        questionService.validateNewQuestion.rejects();
        const res = {} as any as Response;
        res.status = (code) => {
            expect(code).toEqual(HttpStatus.BAD_REQUEST);
            return res;
        };
        res.send = () => res;
        await controller.validateQuestion(new Question(), res);
    });
});
