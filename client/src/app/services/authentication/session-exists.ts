const errorMessage = 'User is already signed in on another device';

export class SessionAlreadyExistsError extends Error {
    code: string;
    constructor(message: string = errorMessage) {
        super(message);
        this.name = 'SessionAlreadyExists';
        this.code = 'SessionAlreadyExists';
    }
}
