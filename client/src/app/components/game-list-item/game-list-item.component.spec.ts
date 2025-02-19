import { ScrollingModule } from '@angular/cdk/scrolling';
import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { By } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { GameListItemComponent } from '@app/components/game-list-item/game-list-item.component';
import { getMockGame } from '@app/constants/game-mocks';
import { GameService } from '@app/services/game/game.service';
import SpyObj = jasmine.SpyObj;

const MOCK_GAME = getMockGame();

describe('GameListItemComponent', () => {
    let component: GameListItemComponent;
    let fixture: ComponentFixture<GameListItemComponent>;
    let gameServiceSpy: SpyObj<GameService>;

    beforeEach(waitForAsync(() => {
        gameServiceSpy = jasmine.createSpyObj('GameService', ['getGames', 'getGameById', 'toggleGameVisibility', 'deleteGame', 'uploadGame']);

        TestBed.configureTestingModule({
            imports: [MatCardModule, HttpClientModule, MatIconModule, RouterModule, RouterTestingModule, ScrollingModule, MatTooltipModule],
            declarations: [GameListItemComponent],
            providers: [{ provide: GameService, useValue: gameServiceSpy }],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListItemComponent);
        component = fixture.componentInstance;
        component.game = MOCK_GAME;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit deleteGameFromList Event when deleteGame is called', () => {
        const spy = spyOn(component.deleteGameFromList, 'emit').and.callThrough();
        component.isAdminMode = true;
        component.deleteGame();
        expect(spy).toHaveBeenCalled();
        expect(spy).toHaveBeenCalledWith(MOCK_GAME.id);
    });

    it('should do nothing if not in admin mode when trying to delete', () => {
        const emitSpy = spyOn(component.deleteGameFromList, 'emit');
        component.isAdminMode = false;
        component.deleteGame();
        expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should display admin buttons if in admin mode', () => {
        component.isAdminMode = true;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css('#icons-container'))).toBeTruthy();
    });

    it('should not display edit, export, and delete buttons if not in admin mode', () => {
        component.isAdminMode = false;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css('#icons-container'))).toBeNull();
    });
});
