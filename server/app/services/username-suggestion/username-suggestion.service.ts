import { englishAdjectives, englishNames, frenchAdjectives, frenchNames, nUsernameSuggestions } from '@app/constants/username-suggestions';
import { FirebaseAuthService } from '@app/modules/firebase/firebase-auth/firebase-auth.service';
import { Injectable } from '@nestjs/common';

@Injectable()
export class UsernameSuggestionService {
    constructor(private authService: FirebaseAuthService) {}

    async getRandomUsernames(language: string) {
        const usernames = [];
        const users = await this.authService.getUsers();
        while (usernames.length < nUsernameSuggestions) {
            let username = this.getRandomUsername(language);
            const nFoundUsers = users.users.filter((user) => user.displayName === username).length;
            if (nFoundUsers > 0) {
                username += length.toString();
            }
            usernames.push(username);
        }
        return usernames;
    }

    getRandomUsername(language: string) {
        if (language === 'fr') {
            const name = frenchNames[this.getRandomIndex(frenchNames)];
            const adjective = frenchAdjectives[this.getRandomIndex(frenchAdjectives)];
            return name + adjective;
        }
        const name = englishNames[this.getRandomIndex(englishNames)];
        const adjective = englishAdjectives[this.getRandomIndex(englishAdjectives)];
        return adjective + name;
    }

    getRandomIndex(strings: string[]) {
        return Math.floor(Math.random() * strings.length);
    }
}
