import { Question } from '@app/interfaces/question';

export interface Game {
    id: string;
    originalId?: string;
    authorId: string;
    authorName: string;
    title: string;
    description: string;
    lastModification: string;
    duration: number;
    isVisible?: boolean;
    nMatchesPlayed?: number;
    questions: Question[];
}
