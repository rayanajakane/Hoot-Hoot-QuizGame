import { Component, OnInit } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    constructor(private translocoService: TranslocoService) {}
    ngOnInit(): void {
        this.translocoService.load('fr').subscribe();
    }
}
