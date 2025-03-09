import { getRandomNumber, getRandomString } from '@app/constants/test-utils';
import { Question } from '@app/interfaces/question';

export const getMockQuestion = (): Question => ({
    id: getRandomString(),
    type: 'QCM',
    text: getRandomString(),
    points: 50,
    choices: [
        {
            text: getRandomString(),
            isCorrect: true,
        },
        {
            text: getRandomString(),
            isCorrect: true,
        },
        {
            text: getRandomString(),
            isCorrect: false,
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
    lastModification: getRandomString(),
    pictureUrl: '',
    pictureFile: null,
    creatorName: '',
});
