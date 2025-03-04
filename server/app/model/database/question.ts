import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';
import { Choice, choiceSchema } from './choice';
import { EstimatedParameters, estimatedParametersSchema } from './estimated-parameters';

export type QuestionDocument = Question & Document;

@Schema()
export class Question {
    @Prop({ required: true })
    id: string;

    @Prop({ required: true })
    type: string;

    @Prop({ required: true })
    text: string;

    @Prop({ required: true })
    points: number;

    @Prop({ type: [choiceSchema], default: [], required: true })
    choices: Choice[];

    @Prop({ type: [estimatedParametersSchema], default: {}, required: true })
    estimatedParameters: EstimatedParameters;

    @Prop({ required: false })
    lastModification: Date;
}

export const questionSchema = SchemaFactory.createForClass(Question);
