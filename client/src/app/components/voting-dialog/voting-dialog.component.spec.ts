import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VotingDialogComponent } from './voting-dialog.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatchRoomService } from '@app/services/match-room/match-room.service';
import { MatchContextService } from '@app/services/match-context/match-context.service';
import { SocketHandlerService } from '@app/services/socket-handler/socket-handler.service';
import spyObj = jasmine.SpyObj;


describe('VotingDialogComponent', () => {
  let component: VotingDialogComponent;
  let fixture: ComponentFixture<VotingDialogComponent>;
  let matchRoomSpy: spyObj<MatchRoomService>;
  let questionContextSpy: spyObj<MatchContextService>;
  let socketSpy: spyObj<SocketHandlerService>;
  questionContextSpy = jasmine.createSpyObj('QuestionContextService', ['getContext']);
  socketSpy = jasmine.createSpyObj('SocketHandlerService', ['emit', 'on']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatSnackBarModule],
      declarations: [VotingDialogComponent],
      providers: [
        {provide: SocketHandlerService, useValue: socketSpy},
        { provide: MatchRoomService, useValue: matchRoomSpy },
        { provide: MatchContextService, useValue: questionContextSpy },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VotingDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
