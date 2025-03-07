import { Choice } from '@app/model/database/choice';
import { EstimatedParameters } from '@app/model/database/estimated-parameters';
import { ApiProperty } from '@nestjs/swagger';
import { IsNumber, IsString } from 'class-validator';

export class UpdateQuestionDto {
    @ApiProperty()
    @IsString()
    id: string;

    @ApiProperty()
    @IsString()
    type: string;

    @ApiProperty()
    @IsString()
    text: string;

    @ApiProperty()
    @IsNumber()
    points: number;

    @ApiProperty()
    choices: Choice[];

    @ApiProperty()
    estimatedParameters: EstimatedParameters;

    @ApiProperty()
    lastModification: Date;
}
