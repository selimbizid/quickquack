import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {Quack} from '../../model/quack';

@Injectable()
export class GlobalService {
    quackListShouldReload: Subject<any> = new Subject<any>();
    quackPosted: Subject<Quack> = new Subject<Quack>();
    quackDeleted: Subject<number> = new Subject<number>();
    userBlocked: Subject<number> = new Subject<number>();
    userBanned: Subject<number> = new Subject<number>();
    aliasChanged: Subject<string> = new Subject<string>();
    
    constructor() { }
}
