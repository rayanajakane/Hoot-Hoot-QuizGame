import { Game } from '@app/model/database/game';
import { Question } from '@app/model/database/question';
import { FirebaseRepositoryService } from '@app/modules/firebase/firebase-repository/firebase-repository.service';
import { Injectable } from '@nestjs/common';
import { GameService } from '../game/game.service';
import { QuestionService } from '../question/question.service';

@Injectable()
export class QuestionPicturesDeletionService {
    constructor(
        private gameService: GameService,
        private questionService: QuestionService,
        private firebaseRepositoryService: FirebaseRepositoryService,
    ) {}

    async deleteGameNonUnsedPictures(game: Game) {
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
                question.pictureUrl === url;
            });
            if (hasSamePicture) gamesWithRequiredPicture.push(game);
        });
        return gamesWithRequiredPicture.length;
    }

    async deleteNonUsedPicture(pictureUrl: string, games: Game[]) {
        const nGamesWithSameImage = this.countGamesSamePicture(pictureUrl, games);
        const nBankQuestionsWithSameImage = await this.questionService.countQuestionsSamePicture(pictureUrl);
        console.log(`nGames: ${nGamesWithSameImage}`);
        console.log(`nBankQuestions: ${nBankQuestionsWithSameImage}`);
        if (nGamesWithSameImage + nBankQuestionsWithSameImage === 0) {
            console.log(pictureUrl);
            await this.firebaseRepositoryService.deleteImage(pictureUrl);
        }
    }
}
