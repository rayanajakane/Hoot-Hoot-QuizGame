// eslint-disable-next-line max-classes-per-file

// TODO : Make so can change message and code
// eslint-disable-next-line max-classes-per-file

export class AuthError extends Error {
    code: string;
    constructor(errorCode: string, errorName: string) {
        super();
        this.name = errorName;
        this.code = errorCode;
    }
}
