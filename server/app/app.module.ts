import { BackupController } from '@app/controllers/backup/backup.controller';
import { GameController } from '@app/controllers/game/game.controller';
import { MatchController } from '@app/controllers/match/match.controller';
import { QuestionController } from '@app/controllers/question/question.controller';
import { AnswerGateway } from '@app/gateways/answer/answer.gateway';
import { ChatGateway } from '@app/gateways/chat/chat.gateway';
import { MatchGateway } from '@app/gateways/match/match.gateway';
import { TimerGateway } from '@app/gateways/timer/timer.gateway';
import { Game, gameSchema } from '@app/model/database/game';
import { Question, questionSchema } from '@app/model/database/question';
import { FirebaseModule } from '@app/modules/firebase/firebase.module';
import { EstimatedAnswerStrategy } from '@app/question-strategies/estimated-answer-strategy/estimated-answer-strategy';
import { LongAnswerStrategy } from '@app/question-strategies/long-answer-strategy/long-answer-strategy';
import { MultipleChoiceStrategy } from '@app/question-strategies/multiple-choice-strategy/multiple-choice-strategy';
import { AnswerService } from '@app/services/answer/answer.service';
import { ChatService } from '@app/services/chat/chat.service';
import { GameCreationService } from '@app/services/game-creation/game-creation.service';
import { GameValidationService } from '@app/services/game-validation/game-validation.service';
import { GameService } from '@app/services/game/game.service';
import { MatchBackupService } from '@app/services/match-backup/match-backup.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { PlayerRoomService } from '@app/services/player-room/player-room.service';
import { QrCodeService } from '@app/services/qr-code/qr-code.service';
import { QuestionPicturesDeletionService } from '@app/services/question-pictures-deletion/question-pictures-deletion.service';
import { QuestionStrategyContext } from '@app/services/question-strategy-context/question-strategy-context.service';
import { QuestionService } from '@app/services/question/question.service';
import { TimeService } from '@app/services/time/time.service';
import { Logger, Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { EventEmitterModule } from '@nestjs/event-emitter';
import { MongooseModule } from '@nestjs/mongoose';
import { QuestionsGeneratorController } from './controllers/questions-generator/questions-generator.controller';
import { QuestionsGeneratorService } from './services/questions-generator/questions-generator.service';

@Module({
    imports: [
        ConfigModule.forRoot({ isGlobal: true }),
        MongooseModule.forRootAsync({
            imports: [ConfigModule],
            inject: [ConfigService],
            useFactory: async (config: ConfigService) => ({
                uri: config.get<string>('DATABASE_CONNECTION_STRING'),
            }),
        }),
        MongooseModule.forFeature([{ name: Game.name, schema: gameSchema }]),
        MongooseModule.forFeature([{ name: Question.name, schema: questionSchema }]),
        EventEmitterModule.forRoot(),
        FirebaseModule,
    ],
    controllers: [GameController, QuestionController, MatchController, BackupController, QuestionsGeneratorController],
    providers: [
        Logger,
        ChatService,
        ChatGateway,
        GameService,
        GameCreationService,
        GameValidationService,
        QuestionService,
        AnswerGateway,
        MatchGateway,
        TimerGateway,
        AnswerService,
        MatchBackupService,
        MatchRoomService,
        PlayerRoomService,
        TimeService,
        QuestionStrategyContext,
        MultipleChoiceStrategy,
        LongAnswerStrategy,
        EstimatedAnswerStrategy,
        QuestionPicturesDeletionService,
        QrCodeService,
        QuestionsGeneratorService,
    ],
})
export class AppModule {}
