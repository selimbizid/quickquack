export class Comment {
    id: number;
    quackId: number;
    authorId: number;
    authorName: string;
    postDate: string;
    content: string;

    constructor(values: Object = {}) {
        Object.assign(this, values);
    }
}
