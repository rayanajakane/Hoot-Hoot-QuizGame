import { getRandomNumber, getRandomString } from '@app/constants/test-utils';
import { Game } from '@app/interfaces/game';

export const getMockGame = (): Game => ({
    id: getRandomString(),
    title: getRandomString(),
    authorId: '',
    authorName: '',
    description: getRandomString(),
    lastModification: new Date().toString(),
    duration: 30,
    isVisible: true,
    questions: [
        {
            id: getRandomString(),
            type: 'QCM',
            text: getRandomString(),
            points: 30,
            choices: [
                {
                    text: getRandomString(),
                    isCorrect: true,
                },
                {
                    text: getRandomString(),
                    isCorrect: false,
                },
            ],
            estimatedParameters: {
                lowerBound: getRandomNumber(1, 2),
                upperBound: getRandomNumber(3, 4),
                margin: 1,
                correctAnswer: getRandomNumber(2, 3),
            },
            lastModification: new Date().toString(),
            pictureUrl: '',
            pictureFile: null,
            creatorName: '',
        },
        {
            id: getRandomString(),
            type: 'QCM',
            text: getRandomString(),
            points: 30,
            choices: [
                {
                    text: getRandomString(),
                    isCorrect: true,
                },
                {
                    text: getRandomString(),
                    isCorrect: false,
                },
            ],
            estimatedParameters: {
                lowerBound: getRandomNumber(1, 2),
                upperBound: getRandomNumber(3, 4),
                margin: 1,
                correctAnswer: getRandomNumber(2, 3),
            },
            lastModification: new Date().toString(),
            pictureUrl: '',
            pictureFile: null,
            creatorName: '',
        },
    ],
});
