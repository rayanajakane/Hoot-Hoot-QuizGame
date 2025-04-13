import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card'; // Import required Angular Material modules
import { MatIconModule } from '@angular/material/icon';
import { EloRankingsComponent } from '@app/components/elo-rankings/elo-rankings.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { EloService } from '@app/services/elo/elo.service';
import { getTranslocoTestingModules } from '@app/transloco-testing.module';

describe('EloRankingsComponent', () => {
    let component: EloRankingsComponent;
    let fixture: ComponentFixture<EloRankingsComponent>;

    const mockAuthenticationService = {
        isUserAuthenticated: jasmine.createSpy('isUserAuthenticated').and.returnValue(true),
    };

    const mockEloService = {
        listenForEloEvents: jasmine.createSpy('listenForEloEvents'),
        getElo: jasmine.createSpy('getElo'),
        getRankings: jasmine.createSpy('getRankings'),
        stopListeningForEloEvents: jasmine.createSpy('stopListeningForEloEvents'),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EloRankingsComponent], // Declare the component here
            imports: [
                CommonModule,
                MatCardModule,
                MatIconModule,
                getTranslocoTestingModules(), // Transloco testing modules
            ],
            providers: [
                { provide: AuthenticationService, useValue: mockAuthenticationService },
                { provide: EloService, useValue: mockEloService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(EloRankingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
