import { Question } from './question';

export interface Game {
    id: string;
    originalId?: string;
    title: string;
    description: string;
    lastModification: string;
    duration: number;
    isVisible?: boolean;
    nMatchesPlayed?: number;
    questions: Question[];
}
