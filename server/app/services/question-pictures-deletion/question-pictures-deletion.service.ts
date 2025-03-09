import { Game } from '@app/model/database/game';
import { Question } from '@app/model/database/question';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { GameService } from '@app/services/game/game.service';
import { QuestionService } from '@app/services/question/question.service';
import { Injectable } from '@nestjs/common';

@Injectable()
export class QuestionPicturesDeletionService {
    constructor(
        private gameService: GameService,
        private questionService: QuestionService,
        private firebaseRepositoryService: FirebaseRepositoryService,
    ) {}

    async deleteGameNonUsedPictures(game: Game) {
        const games = await this.gameService.getAllGames();
        game.questions.forEach(async (question: Question) => {
            if (question.pictureUrl) {
                await this.deleteNonUsedPicture(question.pictureUrl, games);
            }
        });
    }

    countGamesSamePicture(url: string, games: Game[]): number {
        const gamesWithRequiredPicture = [];
        games.forEach((game: Game) => {
            const hasSamePicture = game.questions.some((question: Question) => {
                return question.pictureUrl === url;
            });
            if (hasSamePicture) gamesWithRequiredPicture.push(game);
        });
        return gamesWithRequiredPicture.length;
    }

    async deleteQuestionNonUsedPicture(pictureUrl: string) {
        const games = await this.gameService.getAllGames();
        await this.deleteNonUsedPicture(pictureUrl, games);
    }

    async deleteNonUsedPicture(pictureUrl: string, games: Game[]) {
        if (!pictureUrl) return;
        const nGamesWithSameImage = this.countGamesSamePicture(pictureUrl, games);
        const nBankQuestionsWithSameImage = await this.questionService.countQuestionsSamePicture(pictureUrl);
        if (nGamesWithSameImage + nBankQuestionsWithSameImage === 0) {
            await this.firebaseRepositoryService.deleteImage(pictureUrl);
        }
    }
}
