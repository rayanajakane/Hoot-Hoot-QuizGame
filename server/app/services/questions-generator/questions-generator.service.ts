import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { OnEvent } from '@nestjs/event-emitter';
import { ChatSession, GenerativeModel, GoogleGenerativeAI } from '@google/generative-ai';
import { GenerateQuestionDto } from '@app/model/dto/question/generate-question.dto';
import { v4 } from 'uuid';

const GEMINI_MODEL = "gemini-1.5-flash";

//Sources/tutorials: 
//https://www.youtube.com/watch?v=gBLrCdkZTec&ab_channel=ComputingPower

@Injectable()
export class QuestionsGeneratorService {
    private readonly googleAI: GoogleGenerativeAI;
    private readonly model: GenerativeModel;
    private chatSessions: {[sessionId: string]: ChatSession} = {}; //ChatSession comes from Gemini

    private readonly logger = new Logger(QuestionsGeneratorService.name); 

    
    constructor (configService: ConfigService){
        const geminiApiKey = "AIzaSyDtfJfe29-BN22yt8RDUboSdFb7LXWKHyo";
        this.googleAI = new GoogleGenerativeAI(geminiApiKey);
        this.model = this.googleAI.getGenerativeModel({ model: GEMINI_MODEL });
    }

    async generateText(data: GenerateQuestionDto){
        try{
            const{sessionId,chat} = this.getChatSession(data.sessionId);
            const result = await chat.sendMessage(data.prompt);
            return{
                return: await result.response.text(),
                sessionId
        }
        }
        catch(error){
            this.logger.error("Gemini error", error);
        }
    
    }

    private getChatSession(sessionId?:string){
        let sessionIdToUse = sessionId ?? v4();

        let result = this.chatSessions[sessionIdToUse];

        if(!result){
            result = this.model.startChat();
            this.chatSessions[sessionIdToUse] = result;
        }

        return {
            sessionId: sessionIdToUse,
            chat: result,
        }
    }
}
