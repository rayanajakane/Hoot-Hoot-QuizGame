import { HistoryService } from '@app/services/history/history.service';
import { Controller, Get, HttpStatus, Param, Res } from '@nestjs/common';
import { Response } from 'express';

@Controller('history')
export class HistoryController {
    constructor(private readonly historyService: HistoryService) {}

    @Get('/:id')
    async getUserHistory(@Param('id') id: string, @Res() response: Response) {
        // This could have been done with Firebase client-side.
        // Doing it server-side allows us to reduce logic from client and eventually extend the code if we need to display history for other users.
        try {
            const userHistory = await this.historyService.getHistory(id);
            response.status(HttpStatus.OK).json(userHistory);
        } catch (error) {
            response.status(HttpStatus.NOT_FOUND).send({ message: error });
        }
    }
}
