import {Comment} from './comment';

export class Quack {
    id: number;
    authorName: string;
    authorId: number;
    authorFollowed: boolean;
    authorActive: boolean;
    authorBlocked: boolean;
    content: string;
    postDate: string;
    totalLikes: number;
    comments: Comment[];
    commentsCount: number;
    liked: boolean;
    scope: number;

    constructor(values: Object = {}) {
        Object.assign(this, values);
    }
}
