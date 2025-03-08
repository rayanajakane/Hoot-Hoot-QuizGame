import { HttpClient, HttpHandler } from '@angular/common/http';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { GameListItemComponent } from '@app/components/game-list-item/game-list-item.component';
import { getMockGame } from '@app/constants/game-mocks';
import { AdminMainPageComponent } from '@app/pages/admin-main-page/admin-main-page.component';
import { SortHistoryPipe } from '@app/pipes/sort-history.pipe';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { GameService } from '@app/services/game/game.service';
import { NotificationService } from '@app/services/notification/notification.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { of } from 'rxjs';
import SpyObj = jasmine.SpyObj;

describe('AdminPageComponent', () => {
    let component: AdminMainPageComponent;
    let fixture: ComponentFixture<AdminMainPageComponent>;
    let gameSpy: SpyObj<GameService>;
    let notificationServiceSpy: SpyObj<NotificationService>;
    let dialogMock: SpyObj<MatDialog>;
    let authenticationSpy: SpyObj<AuthenticationService>;

    beforeEach(waitForAsync(() => {
        dialogMock = jasmine.createSpyObj({
            open: jasmine.createSpyObj({
                afterClosed: of('mockResult'),
            }),
        });
        gameSpy = jasmine.createSpyObj('GameService', ['getGames', 'deleteGame', 'uploadGame']);
        authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['isImageToUpload', 'uploadQuestionPicture']);
        notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['displayErrorMessage', 'displaySuccessMessage']);

        gameSpy.games = [getMockGame()];

        TestBed.configureTestingModule({
            imports: [
                MatButtonToggleModule,
                MatMenuModule,
                MatDialogModule,
                MatSnackBarModule,
                RouterTestingModule,
                MatIconModule,
                MatCardModule,
                getTranslocoModule(),
            ],
            declarations: [AdminMainPageComponent, GameListItemComponent, SortHistoryPipe],
            providers: [
                HttpClient,
                HttpHandler,
                { provide: MatDialog, useValue: dialogMock },
                { provide: GameService, useValue: gameSpy },
                { provide: NotificationService, useValue: notificationServiceSpy },
                { provide: AuthenticationService, useValue: authenticationSpy },
            ],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AdminMainPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('onDeleteGameFromList should delete the game', () => {
        component.onDeleteGameFromList('mock');
        expect(gameSpy.deleteGame).toHaveBeenCalledWith('mock');
    });

    it('addGame() should upload the game', () => {
        const mockGame = getMockGame();
        component.addGame(mockGame);
        expect(gameSpy.uploadGame).toHaveBeenCalledWith(mockGame);
    });
});
