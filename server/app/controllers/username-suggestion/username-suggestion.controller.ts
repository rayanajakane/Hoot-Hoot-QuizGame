import { UsernameSuggestionService } from '@app/services/username-suggestion/username-suggestion.service';
import { Controller, Get, HttpStatus, Param, Res } from '@nestjs/common';
import { Response } from 'express';

@Controller('username-suggestion')
export class UsernameSuggestionController {
    constructor(private readonly usernameService: UsernameSuggestionService) {}

    @Get('/:language')
    async getRandomUsernames(@Param('language') language: string, @Res() response: Response) {
        // (Could have used query instead of param but kept param to stay consistent with CommunicationService client-side)
        try {
            const usernames = await this.usernameService.getRandomUsernames(language);
            response.status(HttpStatus.OK).json(usernames);
        } catch (error) {
            response.status(HttpStatus.BAD_REQUEST).send({ message: error });
        }
    }
}
