import { FriendsService } from '@app/services/friends/friends.service';
import { Controller, Delete, Get, HttpStatus, Param, Post, Res } from '@nestjs/common';
import { Response } from 'express';

@Controller('friends')
export class FriendsController {
    constructor(private readonly friendsService: FriendsService) {}

    @Get('all/:userId')
    async getAllUsers(@Param('userId') userId: string, @Res() response: Response) {
        try {
            const allUsers = await this.friendsService.getAllUsers();
            const users = allUsers.filter((user) => user.id !== userId);
            response.status(HttpStatus.OK).json(users);
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }

    @Get('list/:userId')
    async getFriendsList(@Param('userId') userId: string, @Res() response: Response) {
        try {
            const friends = await this.friendsService.getFriendsList(userId);
            response.status(HttpStatus.OK).json(friends);
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }

    @Get('requests/pending/:userId')
    async getPendingRequests(@Param('userId') userId: string, @Res() response: Response) {
        try {
            const pendingRequests = await this.friendsService.getPendingRequests(userId);
            response.status(HttpStatus.OK).json(pendingRequests);
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }

    @Get('requests/sent/:userId')
    async getSentRequests(@Param('userId') userId: string, @Res() response: Response) {
        try {
            const sentRequests = await this.friendsService.getSentRequests(userId);
            response.status(HttpStatus.OK).json(sentRequests);
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }

    @Post('send/:fromUserId/:toUserId')
    async sendFriendRequest(@Param('fromUserId') fromUserId: string, @Param('toUserId') toUserId: string, @Res() response: Response) {
        try {
            await this.friendsService.sendFriendRequest(fromUserId, toUserId);
            response.status(HttpStatus.CREATED).send();
        } catch (error) {
            // TODO: Add error HttpStatus.CONFLICT if friend request already exists
            response.status(HttpStatus.BAD_REQUEST).send({ message: error });
        }
    }

    @Post('accept/:userId/:friendId')
    async acceptFriendRequest(@Param('userId') userId: string, @Param('friendId') friendId: string, @Res() response: Response) {
        try {
            await this.friendsService.acceptFriendRequest(userId, friendId);
            response.status(HttpStatus.OK).send();
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.BAD_REQUEST).send({ message: error });
        }
    }

    @Post('reject/:userId/:friendId')
    async rejectFriendRequest(@Param('userId') userId: string, @Param('friendId') friendId: string, @Res() response: Response) {
        try {
            await this.friendsService.rejectFriendRequest(userId, friendId);
            response.status(HttpStatus.OK).send();
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.BAD_REQUEST).send({ message: error });
        }
    }

    @Delete('cancel/:userId/:friendId')
    async cancelRequest(@Param('userId') userId: string, @Param('friendId') friendId: string, @Res() response: Response) {
        try {
            await this.friendsService.cancelRequest(userId, friendId);
            response.status(HttpStatus.NO_CONTENT).send();
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }

    @Delete('remove/:userId/:friendId')
    async removeFriend(@Param('userId') userId: string, @Param('friendId') friendId: string, @Res() response: Response) {
        try {
            await this.friendsService.removeFriend(userId, friendId);
            response.status(HttpStatus.NO_CONTENT).send();
        } catch (error) {
            // TODO: Add more precise error handling
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }
}
