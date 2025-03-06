import { Choice } from '@app/interfaces/choice';
import { EstimatedParameters } from '@app/interfaces/estimated-parameters';

export interface Question {
    id: string;
    type: string;
    text: string;
    points: number;
    choices?: Choice[];
    answer?: string;
    estimatedParameters?: EstimatedParameters;
    lastModification: string;
    pictureUrl: string;
    pictureFile?: File | null; // To keep the file for uploading question picture AFTER the game / bankQuestion has been validated.
}
