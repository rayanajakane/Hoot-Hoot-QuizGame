import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ManagementState } from '@app/constants/states';
import { Question } from '@app/interfaces/question';
import { GameModificationService } from '@app/services/game-modification/game-modification.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { translate } from '@jsverse/transloco';

@Component({
    selector: 'app-admin-edit-page',
    templateUrl: './admin-edit-page.component.html',
    styleUrl: './admin-edit-page.component.scss',
})
export class AdminEditPageComponent implements OnInit {
    @Output() createQuestionEvent: EventEmitter<Question> = new EventEmitter<Question>();
    @Output() createQuestionEventQuestionBank: EventEmitter<Question> = new EventEmitter<Question>();

    state: ManagementState;

    constructor(
        public gameModificationService: GameModificationService,
        private readonly notificationService: NotificationService,
        private readonly route: ActivatedRoute,
    ) {}

    get game() {
        return this.gameModificationService.game;
    }

    get managementState(): typeof ManagementState {
        return ManagementState;
    }

    ngOnInit() {
        this.getGameIdFromUrl();
    }

    private getGameIdFromUrl() {
        this.route.params.subscribe({
            next: (params) => {
                if (params.id) {
                    this.state = ManagementState.GameModify;
                    this.gameModificationService.setGame(params.id);
                } else {
                    this.state = ManagementState.GameCreate;
                    this.gameModificationService.setNewGame();
                }
            },
            error: (error: HttpErrorResponse) => {
                this.notificationService.displayErrorMessage(`${translate('game-status.failure')}\n${error.message}`);
            },
        });
    }
}
