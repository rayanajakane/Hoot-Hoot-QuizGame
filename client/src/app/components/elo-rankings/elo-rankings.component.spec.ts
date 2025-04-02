import { ComponentFixture, TestBed } from '@angular/core/testing';
import { getTranslocoTestingModules } from '@app/transloco-testing.module';
import { EloRankingsComponent } from './elo-rankings.component';

describe('EloRankingsComponent', () => {
    let component: EloRankingsComponent;
    let fixture: ComponentFixture<EloRankingsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [getTranslocoTestingModules(), EloRankingsComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(EloRankingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
