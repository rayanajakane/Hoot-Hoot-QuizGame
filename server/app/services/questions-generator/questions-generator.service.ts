import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { OnEvent } from '@nestjs/event-emitter';
import { ChatSession, GenerativeModel, GoogleGenerativeAI, Schema, SchemaType } from '@google/generative-ai';

import { GenerateQuestionDto } from '@app/model/dto/question/generate-question.dto';
import { v4 } from 'uuid';

const GEMINI_MODEL = "gemini-1.5-flash";

//Sources/tutorials: 
//https://www.youtube.com/watch?v=gBLrCdkZTec&ab_channel=ComputingPower
//https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/control-generated-output
// https://ai.google.dev/gemini-api/docs/structured-output?lang=node

@Injectable()
export class QuestionsGeneratorService {
    private readonly googleAI: GoogleGenerativeAI;
    private readonly model: GenerativeModel;
    private chatSessions: {[sessionId: string]: ChatSession} = {}; 

    private readonly logger = new Logger(QuestionsGeneratorService.name); 
    schema: Schema = {
        type: SchemaType.OBJECT,
        properties: {
          Questions: {
            type: SchemaType.ARRAY,
            minItems: 8,  
            items: {
              type: SchemaType.OBJECT,
              properties: {
                Question: {
                  type: SchemaType.STRING,
                  nullable: false,
                },
                Numericals: {
                  type: SchemaType.OBJECT,
                  properties: {
                    lowerBound: {
                      type: SchemaType.INTEGER,
                      nullable: false,
                    },
                    upperBound: {
                      type: SchemaType.INTEGER,
                      nullable: false,
                    },
                    exactValue: {
                      type: SchemaType.INTEGER,
                      nullable: false,
                    },
                    errorMargin: {
                      type: SchemaType.INTEGER,
                      nullable: false,
                    },
                  },
                  required: ['lowerBound', 'upperBound', 'exactValue', 'errorMargin'],
                },
                Choices: {
                  type: SchemaType.ARRAY,
                  minItems: 2,
                  items: {
                    type: SchemaType.OBJECT,
                    properties: {
                      Text: {
                        type: SchemaType.STRING,
                        nullable: false,
                      },
                      isCorrect: {
                        type: SchemaType.BOOLEAN,
                        nullable: false,
                      },
                    },
                    required: ['Text', 'isCorrect'],
                  },
                }
              },
              required: ['Question'],
            },
          },
        },
        required: ['Questions'],
 
      };
    constructor (configService: ConfigService){
        const geminiApiKey = configService.get<string>('GEMINI_API_KEY');
        this.googleAI = new GoogleGenerativeAI(geminiApiKey);    
        this.model = this.googleAI.getGenerativeModel({ model: GEMINI_MODEL ,generationConfig :{
            responseMimeType: "application/json",
            responseSchema : this.schema,
        } });
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
