import { ChatGateway } from '@app/gateways/chat/chat.gateway';
import { ChatService } from '@app/services/chat/chat.service';
import { Logger, Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { EventEmitterModule } from '@nestjs/event-emitter';
import { MongooseModule } from '@nestjs/mongoose';
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
        EventEmitterModule.forRoot(),
    ],
    controllers: [],
    providers: [Logger, ChatService, ChatGateway],
})
export class AppModule {}
