import { Component, Input } from '@angular/core';
import { PresetAvatar } from '@app/constants/image-constants';
import { MatchContext } from '@app/constants/states';
import { Player } from '@app/interfaces/player';
import { ChatService } from '@app/services/chat/chat.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { MatchRoomService } from '@app/services/match-room/match-room.service';

@Component({
    selector: 'app-players-list',
    templateUrl: './players-list.component.html',
    styleUrls: ['./players-list.component.scss'],
})
export class PlayersListComponent {
    @Input() players: Player[];
    @Input() canHostToggleChatState: boolean = true;

    defaultAvatar = PresetAvatar.Default;

    context = MatchContext;
    constructor(
        readonly matchRoomService: MatchRoomService,
        readonly chatService: ChatService,
        readonly matchContextService: MatchContextService,
    ) {}
}
