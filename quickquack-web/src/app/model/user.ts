export class User {
    id: number;
    email: string;
    alias: string;
    password: string;
    postsCount: number;
    following: number;
    followed: number;
    followedFromSelf: boolean;
    blockedFromSelf: boolean;
    lastestActivity: string;
    authToken: string;
    permissions: string[];
    roles: string[];
    active: boolean;

    constructor(values: Object = {}) {
        Object.assign(this, values);
    }
}
