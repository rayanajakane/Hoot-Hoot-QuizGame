import { BANNED_PLAYER } from '@app/constants/match-login-errors';
import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player } from '@app/model/schema/player.schema';
import { HistoryService } from '@app/services/history/history.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { PlayerState } from '@common/constants/player-states';
import { AnswerEvents } from '@common/events/answer.events';
import { MatchEvents } from '@common/events/match.events';
import { Feedback } from '@common/interfaces/feedback';
import { Injectable } from '@nestjs/common';
import { Socket } from 'socket.io';
import { v4 as uuidv4 } from 'uuid';

const INDEX_NOT_FOUND = -1;
// const HOST_USERNAME = 'ORGANISATEUR';

@Injectable()
export class PlayerRoomService {
    constructor(
        private readonly matchRoomService: MatchRoomService,
        private historyService: HistoryService,
    ) {}

    getPlayers(code: string): Player[] {
        return this.matchRoomService.getRoom(code).players;
    }

    getPlayersStringified(code: string): string {
        const players = this.getPlayers(code);
        return JSON.stringify(players, (key, value) => {
            if (key !== 'socket') {
                return value;
            }
        });
    }

    recalculateScores(roomCode: string) {
        const totalVotes = Object.values(this.matchRoomService?.votesCount).reduce((total, vote) => total + vote, 0);
        const cheaterUsername = this.matchRoomService.cheaterPlayer.username;
        
        if (totalVotes === this.getPlayers(roomCode).length) {
            const cheaterPlayer = this.matchRoomService.cheaterPlayer;

            if (!this.matchRoomService.cheaterGetsBonus(cheaterUsername)) {
                const cheaterVotes = this.matchRoomService.votesCount[cheaterUsername];

                if (cheaterVotes) {
                    const cheaterScore = this.getPlayerById(roomCode, cheaterPlayer.id).score;
                    const cheaterVoteEntry = this.matchRoomService.totalVotes.filter((vote) => vote.username === cheaterPlayer.username);

                    const playersWhoVotedForCheater = cheaterVoteEntry.flatMap((vote) => vote.usersWhoVoted);

                    playersWhoVotedForCheater.forEach((voterUsername: string) => {
                        const player = this.getPlayerByUsername(roomCode, voterUsername);
                        if (player) {
                            player.bonusCount = Math.round(cheaterScore * 0.3);
                            const players: Player[] = this.getPlayers(roomCode);
                            players.forEach((player: Player) => {
                                player.score = Math.round(player.score + player.bonusCount);
                                const feedback: Feedback = { score: player.score, answerCorrectness: player.answerCorrectness };
                                console.log(feedback);
                                this.getPlayerByUsername(roomCode, voterUsername).socket.emit(AnswerEvents.Feedback, feedback);
                            });
                        }
                    });

                    const players: Player[] = this.getPlayers(roomCode);
                    const player = this.getPlayerByUsername(roomCode, cheaterUsername);
                    player.score = Math.round(player.score - 0.3 * player.score);
                    const feedback: Feedback = { score: player.score, answerCorrectness: player.answerCorrectness };
                    this.getPlayerByUsername(roomCode, player.username).socket.emit(AnswerEvents.Feedback, feedback);
                }
            }
        }

        if (totalVotes === this.getPlayers(roomCode).length) {
            if (this.matchRoomService.cheaterGetsBonus(cheaterUsername)) {
                const player = this.getPlayerByUsername(roomCode, cheaterUsername);
                player.bonusCount = Math.round(player.bonusCount + player.bonusCount * 0.3);
                player.score = Math.round(player.score + player.bonusCount);

                const feedback: Feedback = { score: player.score, answerCorrectness: player.answerCorrectness };
                console.log(feedback);
                this.matchRoomService.cheaterPlayer.socket.emit(AnswerEvents.Feedback, feedback);
            }
        }
    }

    addPlayer(playerSocket: Socket, matchRoomCode: string, newid: string, newUsername: string): Player {
        if (this.getUsernameErrors(matchRoomCode, newUsername)) {
            return undefined;
        }

        const newPlayer: Player = {
            username: newUsername,
            id: newid,
            answer: new MultipleChoiceAnswer(),
            score: 0,
            answerCorrectness: AnswerCorrectness.WRONG,
            nGoodAnswers: 0,
            bonusCount: 0,
            isPlaying: true,
            isChatActive: true,
            socket: playerSocket,
            state: PlayerState.default,
        };
        const matchRoom = this.matchRoomService.getRoom(matchRoomCode);
        matchRoom.players.push(newPlayer);
        matchRoom.activePlayers++;
        return newPlayer;
    }

    deletePlayerBySocket(socketId: string): string {
        let foundPlayer: Player;
        let foundMatchRoom: MatchRoom;
        this.matchRoomService.matchRooms.forEach((matchRoom: MatchRoom) => {
            foundPlayer = matchRoom.players.find((currentPlayer: Player) => {
                return currentPlayer.socket.id === socketId;
            });
            if (!foundMatchRoom) {
                foundMatchRoom = foundPlayer ? matchRoom : undefined;
            }
        });
        if (foundPlayer && foundMatchRoom && !foundMatchRoom.isPlaying) {
            this.deletePlayer(foundMatchRoom.code, foundPlayer.id);
        } else if (foundPlayer && foundMatchRoom && foundMatchRoom.isPlaying) {
            // foundMatchRoom.isPlaying should be false when in results page
            this.makePlayerInactive(foundMatchRoom.code, foundPlayer.id);
            this.historyService.addMatchHistoryItem(foundPlayer.id, {
                id: uuidv4(),
                start: foundMatchRoom.startTime,
                end: new Date(),
                nGoodAnswers: foundPlayer.nGoodAnswers,
                nTotalQuestions: foundMatchRoom.gameLength,
                hasWon: false,
                hasGivenUp: true,
            });
        }
        return foundMatchRoom ? foundMatchRoom.code : undefined;
    }

    getPlayerById(matchRoomCode: string, userId: string): Player | undefined {
        return this.getPlayers(matchRoomCode).find((player: Player) => {
            return player.id === userId;
        });
    }

    getPlayerBySocket(socketId: string): Player | undefined {
        let foundPlayer: Player;
        this.matchRoomService.matchRooms.forEach((matchRoom: MatchRoom) => {
            const playerByMatchRoom = matchRoom.players.find((player: Player) => player.socket.id === socketId);
            if (playerByMatchRoom) {
                foundPlayer = playerByMatchRoom;
                return;
            }
        });
        return foundPlayer;
    }

    getPlayerByUsername(matchRoomCode: string, username: string): Player | undefined {
        const matchRoom = this.matchRoomService.getRoom(matchRoomCode);
        if (matchRoom) {
            return matchRoom.players.find((player: Player) => player.username === username);
        }
        return undefined;
    }

    makePlayerInactive(matchRoomCode: string, userId: string): void {
        const roomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        const playerIndex = this.matchRoomService.getRoom(matchRoomCode).players.findIndex((player: Player) => {
            return player.id === userId;
        });
        if (roomIndex !== INDEX_NOT_FOUND && playerIndex !== INDEX_NOT_FOUND) {
            this.matchRoomService.matchRooms[roomIndex].players[playerIndex].state = PlayerState.exit;
            this.matchRoomService.matchRooms[roomIndex].players[playerIndex].isPlaying = false;
            this.matchRoomService.matchRooms[roomIndex].activePlayers--;
        }
    }

    deletePlayer(matchRoomCode: string, userId: string): void {
        const roomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        this.matchRoomService.matchRooms[roomIndex].activePlayers--;
        this.matchRoomService.matchRooms[roomIndex].players = this.matchRoomService.matchRooms[roomIndex].players.filter((player) => {
            return player.id !== userId;
        });
    }

    getBannedPlayers(matchRoomCode: string): string[] {
        return this.matchRoomService.getRoom(matchRoomCode).bannedIds;
    }

    getActivePlayers(matchRoomCode: string): Player[] {
        return this.getPlayers(matchRoomCode).filter((player: Player) => {
            return player.state !== PlayerState.exit;
        });
    }

    addBannedPlayers(matchRoomCode: string, userId: string) {
        const room = this.matchRoomService.getRoom(matchRoomCode);
        if (room) {
            room.bannedIds.push(userId);
        }
    }

    isBannedPlayer(matchRoomCode: string, userId: string): boolean {
        const bannedUsernames = this.getBannedPlayers(matchRoomCode);
        const idIndex = bannedUsernames.findIndex((id: string) => {
            return id === userId;
        });
        return idIndex !== INDEX_NOT_FOUND;
    }

    isHostPlayer(matchRoomCode: string): boolean {
        return !!this.getPlayerById(matchRoomCode, this.matchRoomService.getRoom(matchRoomCode).hostId);
    }

    getUsernameErrors(matchRoomCode: string, userId: string): string {
        let errors = '';
        const errorConditions: Map<string, boolean> = new Map([[BANNED_PLAYER, this.isBannedPlayer(matchRoomCode, userId)]]);
        errorConditions.forEach((hasError: boolean, message: string) => {
            if (hasError) errors += message;
        });
        return errors;
    }

    setStateForAll(matchRoomCode: string, state: string): void {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        this.matchRoomService.matchRooms[matchRoomIndex].players.forEach((player: Player) => {
            if (player.state !== PlayerState.exit) {
                player.state = state;
                return player;
            }
        });
        this.sendPlayersToHost(matchRoomCode);
    }

    setState(socketId: string, state: string): void {
        let foundPlayerIndex = INDEX_NOT_FOUND;
        let foundMatchRoomIndex = INDEX_NOT_FOUND;
        this.matchRoomService.matchRooms.forEach((matchRoom: MatchRoom, currentIndex: number) => {
            foundPlayerIndex = matchRoom.players.findIndex((currentPlayer: Player) => {
                return currentPlayer.socket.id === socketId;
            });
            if (foundMatchRoomIndex === INDEX_NOT_FOUND) {
                foundMatchRoomIndex = foundPlayerIndex !== INDEX_NOT_FOUND ? currentIndex : INDEX_NOT_FOUND;
            }
        });
        if (foundPlayerIndex !== INDEX_NOT_FOUND && foundMatchRoomIndex !== INDEX_NOT_FOUND) {
            this.matchRoomService.matchRooms[foundMatchRoomIndex].players[foundPlayerIndex].state = state;
            this.sendPlayersToHost(this.matchRoomService.matchRooms[foundMatchRoomIndex].code);
        }
    }

    sendPlayersToHost(matchRoomCode: string) {
        const matchRoomIndex = this.matchRoomService.getRoomIndex(matchRoomCode);
        this.matchRoomService.matchRooms[matchRoomIndex].hostSocket.emit(MatchEvents.FetchPlayersData, this.getPlayersStringified(matchRoomCode));
    }
}
