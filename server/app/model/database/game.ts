import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { ApiProperty } from '@nestjs/swagger';
import { Document } from 'mongoose';
import { Question, questionSchema } from './question';

export type GameDocument = Game & Document;

@Schema()
export class Game {
    @ApiProperty()
    @Prop({ required: true })
    id: string;

    @ApiProperty()
    @Prop({ required: true })
    originalId: string; // Used for game popularity feature (id cannot be reused because of backup game feature)

    @ApiProperty()
    @Prop({ required: true })
    title: string;

    @ApiProperty()
    @Prop({ required: true })
    description: string;

    @ApiProperty()
    @Prop({ required: true })
    lastModification: Date;

    @ApiProperty()
    @Prop({ required: true })
    duration: number;

    @ApiProperty()
    @Prop({ required: true, default: true })
    isVisible: boolean;

    @Prop({ required: true, default: 0 })
    nMatchesPlayed: number;

    @Prop({ type: [questionSchema], default: [], required: true })
    questions: Question[];
}

export const gameSchema = SchemaFactory.createForClass(Game);
