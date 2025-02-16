import { TestBed } from '@angular/core/testing';
import { AppRoutingModule } from '@app/modules/app-routing.module';
import { AppComponent } from '@app/pages/app/app.component';
import { AuthenticationService } from '@app/services/authentication/authentication.service';
import { getTranslocoModule } from '@app/transloco-testing.module';
import { mockProvider } from '@ngneat/spectator';

describe('AppComponent', () => {
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [AppRoutingModule, getTranslocoModule()],
            providers: [mockProvider(AuthenticationService)],
            declarations: [AppComponent],
        }).compileComponents();
    });

    it('should create the app', () => {
        const fixture = TestBed.createComponent(AppComponent);
        const app = fixture.componentInstance;
        expect(app).toBeTruthy();
    });
});
