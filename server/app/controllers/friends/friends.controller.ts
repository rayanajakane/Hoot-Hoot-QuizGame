import { FriendsService } from '@app/services/friends/friends.service';
import { UserIdName } from '@common/interfaces/user-id-name';
import { Controller, Delete, Get, Param, Post } from '@nestjs/common';

@Controller('friends')
export class FriendsController {
    constructor(private readonly friendsService: FriendsService) {}

    @Get('all/:userId')
    async getAllUsers(@Param('userId') userId: string): Promise<UserIdName[]> {
        const allUsers = await this.friendsService.getAllUsers();
        return allUsers.filter((user) => user.id !== userId);
    }

    @Get('list/:userId')
    async getFriendsList(@Param('userId') userId: string): Promise<UserIdName[]> {
        return this.friendsService.getFriendsList(userId);
    }

    @Get('requests/pending/:userId')
    async getPendingRequests(@Param('userId') userId: string): Promise<UserIdName[]> {
        return this.friendsService.getPendingRequests(userId);
    }

    @Get('requests/sent/:userId')
    async getSentRequests(@Param('userId') userId: string): Promise<UserIdName[]> {
        return this.friendsService.getSentRequests(userId);
    }

    @Post('send/:fromUserId/:toUserId')
    async sendFriendRequest(@Param('fromUserId') fromUserId: string, @Param('toUserId') toUserId: string): Promise<void> {
        return this.friendsService.sendFriendRequest(fromUserId, toUserId);
    }

    @Post('accept/:userId/:friendId')
    async acceptFriendRequest(@Param('userId') userId: string, @Param('friendId') friendId: string): Promise<void> {
        return this.friendsService.acceptFriendRequest(userId, friendId);
    }

    @Post('reject/:userId/:friendId')
    async rejectFriendRequest(@Param('userId') userId: string, @Param('friendId') friendId: string): Promise<void> {
        return this.friendsService.rejectFriendRequest(userId, friendId);
    }

    @Delete('remove/:userId/:friendId')
    async removeFriend(@Param('userId') userId: string, @Param('friendId') friendId: string): Promise<void> {
        return this.friendsService.removeFriend(userId, friendId);
    }
}
