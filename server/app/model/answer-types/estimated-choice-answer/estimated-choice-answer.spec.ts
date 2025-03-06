import { EstimatedAnswer } from './estimated-choice-answer';

describe('EstimatedAnswer', () => {
    let estimatedAnswer: EstimatedAnswer;

    beforeEach(() => {
        estimatedAnswer = new EstimatedAnswer();
    });

    it('should be defined', () => {
        expect(estimatedAnswer).toBeDefined();
    });
});
