import { Choice } from '@app/model/database/choice';
import { Game } from '@app/model/database/game';
import { getMockQuestion, VALID_QUESTION } from './question-mocks';
import { getRandomString } from './test-utils';

const gameYear = 2020;

const getMockGame = (): Game => ({
    id: getRandomString(),
    originalId: getRandomString(),
    title: getRandomString(),
    description: getRandomString(),
    lastModification: new Date(gameYear, 1, 1),
    duration: 30,
    isVisible: true,
    nMatchesPlayed: 0,
    questions: [getMockQuestion()],
});

const MOCK_CHOICES: Choice[] = [
    { text: 'correct choice', isCorrect: true },
    { text: 'incorrect choice', isCorrect: false },
];
const GAME_VALID_QUESTION = getMockGame();
GAME_VALID_QUESTION.questions = [VALID_QUESTION];

const pastYear = 2020;
const GAME_WITH_IS_CORRECT_FIELD: Game = {
    id: '',
    originalId: '',
    title: '',
    description: '',
    lastModification: new Date(pastYear, 1, 1),
    duration: 0,
    isVisible: true,
    nMatchesPlayed: 0,
    questions: [
        {
            id: '0',
            type: 'QCM',
            text: '',
            points: 0,
            lastModification: new Date(pastYear, 1, 1),
            choices: [
                {
                    text: '',
                    isCorrect: true,
                },
                {
                    text: '',
                    isCorrect: false,
                },
            ],
            estimatedParameters: {
                lowerBound: 0,
                upperBound: 1,
                margin: 0,
                correctAnswer: 0,
            },
            pictureUrl: '',
            creatorName: 'mock',
        },
        {
            id: '1',
            type: 'QCM',
            text: '',
            points: 0,
            lastModification: new Date(pastYear, 1, 1),
            choices: [
                {
                    text: '',
                    isCorrect: true,
                },
                {
                    text: '',
                    isCorrect: false,
                },
            ],
            estimatedParameters: {
                lowerBound: 0,
                upperBound: 1,
                margin: 0,
                correctAnswer: 0,
            },
            pictureUrl: '',
            creatorName: 'mock',
        },
    ],
};

const GAME_WITHOUT_IS_CORRECT_FIELD: Game = {
    id: '',
    originalId: '',
    title: '',
    description: '',
    lastModification: new Date(pastYear, 1, 1),
    duration: 0,
    isVisible: true,
    nMatchesPlayed: 0,
    questions: [
        {
            id: '0',
            type: 'QCM',
            text: '',
            points: 0,
            lastModification: new Date(pastYear, 1, 1),
            choices: [
                {
                    text: '',
                },
                {
                    text: '',
                },
            ],
            estimatedParameters: {
                lowerBound: 0,
                upperBound: 1,
                margin: 0,
                correctAnswer: 0,
            },
            pictureUrl: '',
            creatorName: 'mock',
        },
        {
            id: '1',
            type: 'QCM',
            text: '',
            points: 0,
            lastModification: new Date(pastYear, 1, 1),
            choices: [
                {
                    text: '',
                },
                {
                    text: '',
                },
            ],
            estimatedParameters: {
                lowerBound: 0,
                upperBound: 1,
                margin: 0,
                correctAnswer: 0,
            },
            pictureUrl: '',
            creatorName: 'mock',
        },
    ],
};

export { GAME_VALID_QUESTION, GAME_WITH_IS_CORRECT_FIELD, GAME_WITHOUT_IS_CORRECT_FIELD, getMockGame, MOCK_CHOICES };
