import { FriendsService } from '@app/services/friends/friends.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MoneyService } from '@app/services/money/money.service';
import { Injectable } from '@nestjs/common';

@Injectable()
export class PartyService {
    constructor(
        private readonly moneyService: MoneyService,
        private readonly friendService: FriendsService,
        private readonly matchRoomService: MatchRoomService,
    ) {}

    async canJoinParty(userId: string, roomCode: string): Promise<string[]> {
        const matchRoom = this.matchRoomService.getRoom(roomCode);
        let errors = [];

        if (matchRoom.partyConfig.isFriendsOnly) {
            const friendshipErrors = await this.friendService.getFriendshipErrors(matchRoom.hostId, false, userId);
            if (friendshipErrors.length > 0) {
                errors = errors.concat(friendshipErrors);
            }
        }

        if (matchRoom.partyConfig.isEntryFeeRequired) {
            const moneyErrors = await this.moneyService.getMoneyError(userId, matchRoom.partyConfig.entryFeeAmount);
            if (moneyErrors) errors = errors.concat(moneyErrors);
        }

        return errors;
    }

    async joinParty(userId: string, roomCode: string): Promise<void> {
        const matchRoom = this.matchRoomService.getRoom(roomCode);
        await this.moneyService.updateBalance(userId, -matchRoom.partyConfig.entryFeeAmount);
    }

    async leaveParty(userId: string, roomCode: string): Promise<void> {
        const matchRoom = this.matchRoomService.getRoom(roomCode);
        await this.moneyService.updateBalance(userId, matchRoom.partyConfig.entryFeeAmount);
    }
}
