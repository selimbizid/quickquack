import {Injectable} from '@angular/core';
import {Http, URLSearchParams} from '@angular/http';
import {Observable} from 'rxjs/Rx';
import {UserService} from '../user/user.service';
import {Quack} from '../../model/quack';
import {Comment} from '../../model/comment';

@Injectable()
export class QuackService {
    private serviceUrl = 'http://localhost:8080/quickquack/rest/quack';

    constructor(private http: Http, private uService: UserService) {}

    get(offset: number = 0, since: number = 0): Observable<Quack[]> {
        return this.http
            .get(this.serviceUrl, {headers: this.uService.getHeaders({consumes:'application/json'}), params: {'from': offset, 'limit': 10, 'since':since}})
            .map(r => r.json() as Quack[]);
    }

    getComments(quackId: number, offset: number = 0): Observable<Comment[]> {
        return this.http
            .get(this.serviceUrl + '/comment', {headers: this.uService.getHeaders({consumes:'application/json'}), params: {'quackId': quackId, 'from': offset}})
            .map(r => r.json() as Comment[]);
    }

    putComment(comment: Comment): Observable<Comment> {
        return this.http
            .put(this.serviceUrl + '/comment', JSON.stringify(comment), {headers: this.uService.getHeaders({consumes:'application/json', produces:'application/json'})})
            .map(r => r.json() as Comment);
    }
    
    removeComment(commId: number): Observable<boolean> {
        return this.http
            .delete(this.serviceUrl + '/comment', {headers: this.uService.getHeaders({}), params: {'id': commId}})
            .map(r => r.ok);
    }

    findByHashtag(tag: string, offset: number): Observable<Quack[]> {
        return this.http
            .get(this.serviceUrl + '/search', {headers: this.uService.getHeaders({consumes:'application/json'}), params: {'tag': tag, 'from': offset}})
            .map(r => r.json() as Quack[]);
    }

    findById(id: number): Observable<Quack> {
        return this.http
            .get(this.serviceUrl + '/get', {headers: this.uService.getHeaders({consumes:'application/json'}), params: {'id': id}})
            .map(r => r.json() as Quack);
    }
    
    findByUserId(id: number, offset: number, since: number = 0): Observable<Quack[]> {
        return this.http
            .get(this.serviceUrl + '/user', {headers: this.uService.getHeaders({consumes:'application/json'}), params: {'id': id, 'from': offset, 'since':since}})
            .map(r => r.json() as Quack[]);
    }

    put(quack: Quack): Observable<Quack> {
        return this.http
            .put(this.serviceUrl + '/put', JSON.stringify(quack), {headers: this.uService.getHeaders({consumes:'application/json', produces:'application/json'})})
            .map(r => r.json() as Quack);
    }

    edit(quack: Quack): Observable<boolean> {
        return this.http
            .post(this.serviceUrl + '/edit', JSON.stringify(quack), {headers: this.uService.getHeaders({produces:'application/json'})})
            .map(r => r.ok);
    }

    remove(quackId: number): Observable<boolean> {
        return this.http
            .delete(this.serviceUrl, {headers: this.uService.getHeaders('text/plain'), params: {'id': quackId}})
            .map(r => r.ok);
    }

    like(quackId: number, yes: boolean = true): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('id', quackId.toString());
        body.append('yes', yes ? 'true' : 'false');
        return this.http
            .post(this.serviceUrl + '/like', body, {headers: this.uService.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }
    
    changeScope(quackId: number, scope: number): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('quackId', quackId.toString());
        body.append('scope', scope.toString());
        return this.http
            .post(this.serviceUrl + '/scope', body, {headers: this.uService.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }
}
