export class Permission {
    id: number;
    identifier: string;

    constructor(values: Object = {}) {
        Object.assign(this, values);
    }
}
