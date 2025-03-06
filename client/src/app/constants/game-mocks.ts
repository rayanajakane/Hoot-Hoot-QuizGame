import { Game } from '@app/interfaces/game';
import { getRandomNumber, getRandomString } from './test-utils';

export const getMockGame = (): Game => ({
    id: getRandomString(),
    title: getRandomString(),
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
        },
    ],
});
