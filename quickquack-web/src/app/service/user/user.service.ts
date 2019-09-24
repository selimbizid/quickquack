import {Injectable} from '@angular/core';
import {Http, Headers, URLSearchParams} from '@angular/http';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import {User} from '../../model/user';

@Injectable()
export class UserService {
    private serviceUrl = 'http://localhost:8080/quickquack/rest/user';

    constructor(private http: Http) {}

    login(email: string, password: string): Observable<User> {
        let body = new URLSearchParams
        body.append('email', email);
        body.append('password', password);
        let obs = this.http
            .post(this.serviceUrl + '/login', body, {headers: this.getHeaders({consumes: 'application/json', produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.json() as User);
        return obs;
    }

    getById(id: number): Observable<User> {
        return this.http
            .get(this.serviceUrl + '/get', {headers: this.getHeaders({consumes: 'application/json'}), params: {'id': id}})
            .map(r => r.json() as User);
    }

    find(containing: string, offset: number): Observable<User[]> {
        return this.http
            .get(this.serviceUrl + '/find', {headers: this.getHeaders({consumes: 'application/json'}), params: {'containing': containing, 'from':offset}})
            .map(r => r.json() as User[]);
    }

    findAll(): Observable<User[]> {
        return this.http
            .get(this.serviceUrl + '', {headers: this.getHeaders({consumes: 'application/json'})})
            .map(r => r.json() as User[]);
    }

    block(id: string, block: string = 'true'): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('id', id);
        body.append('block', block);
        return this.http
            .post(this.serviceUrl + '/block', body, {headers: this.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }

    register(email: string, password: string): Observable<boolean> {
        let body = new URLSearchParams;
        body.append('email', email);
        body.append('password', password);
        return this.http
            .post(this.serviceUrl + '/register', body, {headers: this.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }

    changePassword(current: string, newPass: string): Observable<boolean> {
        let body = new URLSearchParams;
        body.append('old', current);
        body.append('new', newPass);
        return this.http
            .post(this.serviceUrl + '/changePassword', body, {headers: this.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => {
                if (r.ok) {
                    localStorage.setItem('authToken', r.text());
                    return true;
                } else return false;
            });
    }

    changeAlias(alias: string): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('alias', alias);
        return this.http
            .post(this.serviceUrl + '/changeAlias', body, {headers: this.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => {
                if (r.ok) {
                    localStorage.setItem('currentAlias', alias);
                }
                return r.ok;
            });
    }

    toggleFollow(id: number): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('id', id.toString());
        return this.http
            .post(this.serviceUrl + '/follow', body, {headers: this.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }

    getHeaders({produces = 'text/plain', consumes = 'text/plain'}): Headers {
        let headers = new Headers();
        headers.append('Accept', consumes);
        headers.append('Content-Type', produces);
        let authToken = localStorage.getItem('authToken');
        if (authToken) {
            headers.append('Authorization', authToken);
        }
        return headers;
    }
}
