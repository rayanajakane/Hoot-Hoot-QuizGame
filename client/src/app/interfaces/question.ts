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
}
