import { GenerateQuestionDto } from '@app/model/dto/question/generate-question.dto';
import { QuestionsGeneratorService } from '@app/services/questions-generator/questions-generator.service';
import { Body, Controller, Post, UsePipes, ValidationPipe } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';



@ApiTags('questions')
@Controller('questions')
export class QuestionsGeneratorController {
    constructor(private service: QuestionsGeneratorService) {}

    @Post('/generate-question')
    @UsePipes(new ValidationPipe({ transform: true })) 
    getResponse(@Body() data: GenerateQuestionDto) { 
        return this.service.generateText(data);
    }
}