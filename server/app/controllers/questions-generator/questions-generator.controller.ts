import { GenerateQuestionDto } from '@app/model/dto/question/generate-question.dto';
import { QuestionsGeneratorService } from '@app/services/questions-generator/questions-generator.service';
import { Body, Controller, Post, UsePipes, ValidationPipe } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';

//Maybe it's best to add this to questions controller

@ApiTags('questions')
@Controller('questions')
export class QuestionsGeneratorController {
    constructor(private service: QuestionsGeneratorService) {}

    @Post('/generate-question')
    @UsePipes(new ValidationPipe({ transform: true })) // so we cna have to the methods of the class dto not just the properties
    getResponse(@Body() data: GenerateQuestionDto) { //receives a plain json file
        return this.service.generateText(data);
    }
}