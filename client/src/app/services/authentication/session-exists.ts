// eslint-disable-next-line max-classes-per-file
const errorMessage = 'User is already signed in on another device';

// TODO : Make so can change message and code
export class SessionAlreadyExistsError extends Error {
    code: string;
    constructor(message: string = errorMessage) {
        super(message);
        this.name = 'SessionAlreadyExists';
        this.code = 'SessionAlreadyExists';
    }
}

export class UsernameAlreadyExistsError extends Error {
    code: string;
    constructor(message: string = errorMessage) {
        super(message);
        this.name = 'UsernameAlreadyExists';
        this.code = 'UsernameAlreadyExists';
    }
}
