import { MultipleChoiceAnswer } from '@app/model/answer-types/multiple-choice-answer/multiple-choice-answer';
import { MatchRoom } from '@app/model/schema/match-room.schema';
import { Player } from '@app/model/schema/player.schema';
import { ChoiceTracker } from '@app/model/tally-trackers/choice-tracker/choice-tracker';
import { AnswerCorrectness } from '@common/constants/answer-correctness';
import { PlayerState } from '@common/constants/player-states';
import { MOCK_MESSAGE } from './chat-mocks';
import { getMockGame } from './game-mocks';
import { getMockQuestion } from './question-mocks';

const MOCK_USER_INFO = { roomCode: '', username: '' };
const MOCK_MESSAGE_INFO = { roomCode: '', message: MOCK_MESSAGE };
const MOCK_PLAYER: Player = {
    username: '',
    id: '',
    answer: new MultipleChoiceAnswer(),
    score: 0,
    answerCorrectness: AnswerCorrectness.WRONG,
    bonusCount: 0,
    isPlaying: true,
    isChatActive: true,
    socket: undefined,
    state: PlayerState.default,
};
const MOCK_MATCH_ROOM: MatchRoom = {
    code: '',
    isLocked: false,
    isPlaying: false,
    game: getMockGame(),
    gameLength: 1,
    questionDuration: 60,
    currentQuestion: getMockQuestion(),
    currentQuestionIndex: 0,
    currentQuestionAnswer: [],
    choiceTracker: new ChoiceTracker(),
    matchHistograms: [],
    bannedIds: [],
    players: [],
    activePlayers: 0,
    submittedPlayers: 0,
    messages: [],
    hostSocket: undefined,
    isClassicMode: true,
    startTime: new Date(),
    qrCodeUrl: '',
    hostId: '',
};

const MOCK_TEST_MATCH_ROOM: MatchRoom = {
    code: '',
    isLocked: false,
    isPlaying: false,
    game: getMockGame(),
    gameLength: 1,
    questionDuration: 60,
    currentQuestion: getMockQuestion(),
    currentQuestionIndex: 0,
    currentQuestionAnswer: [],
    choiceTracker: new ChoiceTracker(),
    matchHistograms: [],
    bannedIds: [],
    players: [],
    activePlayers: 0,
    submittedPlayers: 0,
    messages: [],
    hostSocket: undefined,
    isClassicMode: true,
    startTime: new Date(),
    qrCodeUrl: '',
    hostId: '',
};

const MOCK_RANDOM_MATCH_ROOM: MatchRoom = {
    code: '',
    isLocked: false,
    isPlaying: false,
    game: getMockGame(),
    gameLength: 1,
    questionDuration: 60,
    currentQuestion: getMockQuestion(),
    currentQuestionIndex: 0,
    currentQuestionAnswer: [],
    choiceTracker: new ChoiceTracker(),
    matchHistograms: [],
    bannedIds: [],
    players: [],
    activePlayers: 0,
    submittedPlayers: 0,
    messages: [],
    hostSocket: undefined,
    isClassicMode: true,
    startTime: new Date(),
    qrCodeUrl: '',
    hostId: '',
};

const MOCK_PLAYER_ROOM: MatchRoom = {
    code: '',
    isLocked: false,
    isPlaying: false,
    game: getMockGame(),
    gameLength: 1,
    questionDuration: 60,
    currentQuestion: getMockQuestion(),
    currentQuestionIndex: 0,
    currentQuestionAnswer: [],
    choiceTracker: new ChoiceTracker(),
    matchHistograms: [],
    bannedIds: [],
    players: [MOCK_PLAYER],
    activePlayers: 1,
    submittedPlayers: 0,
    messages: [],
    hostSocket: undefined,
    isClassicMode: true,
    startTime: new Date(),
    qrCodeUrl: '',
    hostId: '',
};

const MOCK_ROOM_CODE = 'mockCode';
const MOCK_USERNAME = 'mockUsername';

export {
    MOCK_MATCH_ROOM,
    MOCK_MESSAGE,
    MOCK_MESSAGE_INFO,
    MOCK_PLAYER,
    MOCK_PLAYER_ROOM,
    MOCK_RANDOM_MATCH_ROOM,
    MOCK_ROOM_CODE,
    MOCK_TEST_MATCH_ROOM,
    MOCK_USER_INFO,
    MOCK_USERNAME,
};
