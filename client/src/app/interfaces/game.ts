import { Question } from './question';

// TODO : Make extend from server-side interface to keep both the same
export interface Game {
    id: string;
    title: string;
    description: string;
    lastModification: string;
    duration: number;
    isVisible?: boolean;
    questions: Question[];
}
