import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

export type EstimatedParametersDocument = EstimatedParameters & Document;

@Schema()
export class EstimatedParameters {
    @Prop({ required: true })
    correctAnswer: number;

    @Prop({ required: true })
    lowerBound: number;

    @Prop({ required: true })
    upperBound: number;

    @Prop({ required: true })
    margin: number;
}

export const estimatedParametersSchema = SchemaFactory.createForClass(EstimatedParameters);
