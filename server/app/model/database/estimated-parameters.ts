import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

export type EstimatedParametersDocument = EstimatedParameters & Document;

@Schema()
export class EstimatedParameters {
    @Prop({ required: false })
    correctAnswer?: number;

    @Prop({ required: false })
    lowerBound?: number;

    @Prop({ required: false })
    upperBound?: number;

    @Prop({ required: false })
    margin?: number;
}

export const estimatedParametersSchema = SchemaFactory.createForClass(EstimatedParameters);
