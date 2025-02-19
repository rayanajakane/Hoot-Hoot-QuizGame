import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { NotificationService } from '@app/services/notification/notification.service';

export const matchLoginGuard = (): boolean => {
    const matchRoomService = inject(MatchRoomService);
    const router = inject(Router);
    const notificationService = inject(NotificationService);

    const isPlaying = matchRoomService.isPlaying;
    const hasRoomCode = !!matchRoomService.getRoomCode();
    const hasUsername = !!matchRoomService.getUsername();

    if (!hasRoomCode || !hasUsername || isPlaying) {
        router.navigateByUrl('/home');
        notificationService.displayErrorMessage('Accès refusé: Veuillez rejoindre une partie ou créer une partie.');
        return false;
    }

    return true;
};
