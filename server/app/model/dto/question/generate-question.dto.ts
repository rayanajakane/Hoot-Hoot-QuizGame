import { IsNotEmpty, IsOptional, IsString } from "class-validator";

export class GenerateQuestionDto {
    @IsString()
    @IsNotEmpty()
    prompt: string;

    @IsString()
    @IsOptional()
    sessionId?:string; //will allow to control multiple chat sessions with the app
}