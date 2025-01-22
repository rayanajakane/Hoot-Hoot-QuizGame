import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '@app/services/authentication/authentication.service';

export const authenticationGuard = (): boolean => {
    const authenticationService = inject(AuthenticationService);
    const router = inject(Router);
    if (!authenticationService.userDisplayName) {
        router.navigateByUrl('/login');
        return false;
    }
    return true;
};
