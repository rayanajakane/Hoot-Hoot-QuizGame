import { Game } from '@app/model/database/game';
import { Question } from '@app/model/database/question';
import { ChoiceTracker } from '@app/model/tally-trackers/choice-tracker/choice-tracker';
import { Histogram } from '@common/interfaces/histogram';
import { Socket } from 'socket.io';
import { Message } from './message.schema';
import { Player } from './player.schema';

export interface MatchRoom {
    code: string;
    isLocked: boolean;
    isPlaying: boolean;
    game: Game;
    gameLength: number;
    questionDuration: number;
    currentQuestion: Question;
    currentQuestionIndex: number;
    currentQuestionAnswer: string[];
    choiceTracker: ChoiceTracker;
    matchHistograms: Histogram[];
    bannedUsernames: string[];
    players: Player[];
    activePlayers: number;
    submittedPlayers: number;
    messages: Message[];
    hostSocket: Socket;
    isClassicMode: boolean;
    startTime: Date;
    qrCodeUrl: string;
}
