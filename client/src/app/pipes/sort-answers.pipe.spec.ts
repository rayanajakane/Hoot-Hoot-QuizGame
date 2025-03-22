import { SortAnswersPipe } from '@app/pipes/sort-answers.pipe';
import { LongAnswerInfo } from '@common/interfaces/long-answer-info';

describe('SortAnswersPipe', () => {
    let pipe: SortAnswersPipe;

    beforeEach(() => {
        pipe = new SortAnswersPipe();
    });

    it('create an instance', () => {
        expect(pipe).toBeTruthy();
    });

    it('should sort answers by username', () => {
        const input: LongAnswerInfo[] = [
            { userId: '123', username: 'alice', answer: 'Answer 1', score: '0' },
            { userId: '231', username: 'charlie', answer: 'Answer 3', score: '0' },
            { userId: '321', username: 'bob', answer: 'Answer 2', score: '0' },
        ];

        const sorted = pipe.transform(input);

        expect(sorted.length).toBe(3);
        expect(sorted[0].username).toBe('alice');
        expect(sorted[1].username).toBe('bob');
        expect(sorted[2].username).toBe('charlie');
    });

    it('should not be case sensitive', () => {
        const input: LongAnswerInfo[] = [
            { userId: '123', username: 'AlIcE1', answer: 'Answer 1', score: '0' },
            { userId: '231', username: 'charlie', answer: 'Answer 3', score: '0' },
            { userId: '321', username: 'BOB', answer: 'Answer 2', score: '0' },
        ];

        const sorted = pipe.transform(input);

        expect(sorted.length).toBe(3);
        expect(sorted[0].username).toBe('AlIcE1');
        expect(sorted[1].username).toBe('BOB');
        expect(sorted[2].username).toBe('charlie');
    });

    it('should return empty array for empty input', () => {
        const sorted = pipe.transform([] as LongAnswerInfo[]);

        expect(sorted).toEqual([]);
    });
});
