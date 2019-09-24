import {Injectable} from '@angular/core';
import {Http, URLSearchParams} from '@angular/http';
import {Observable} from 'rxjs/Rx';
import {UserService} from '../user/user.service';
import {Role} from '../../model/role';
import {Permission} from '../../model/permission';

@Injectable()
export class AdministrationService {
    private serviceUrl = 'http://localhost:8080/quickquack/rest/admin';

    constructor(private http: Http, private userService: UserService) {}

    getRoles(): Observable<Role[]> {
        return this.http
            .get(this.serviceUrl + '/role', {headers: this.userService.getHeaders({consumes:'application/json'})})
            .map(list => list.json() as Role[]);
    }

    getPermissions(): Observable<Permission[]> {
        return this.http
            .get(this.serviceUrl + '/permission', {headers: this.userService.getHeaders({consumes:'application/json'})})
            .map(list => list.json() as Permission[]);
    }

    putRole(roleName: string): Observable<boolean> {
        return this.http
            .put(this.serviceUrl + '/role', {headers: this.userService.getHeaders({}), params: {'name': roleName}})
            .map(r => r.ok);
    }

    putPermission(permName: string): Observable<boolean> {
        return this.http
            .put(this.serviceUrl + '/permission', {headers: this.userService.getHeaders({}), params: {'name': permName}})
            .map(r => r.ok);
    }

    deletePermission(permName: string): Observable<boolean> {
        return this.http
            .delete(this.serviceUrl + '/permission', {headers: this.userService.getHeaders({}), params: {'name': permName}})
            .map(r => r.ok);
    }

    assign(roleName: string, permName: string): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('role', roleName);
        body.append('permission', permName);
        return this.http
            .post(this.serviceUrl + '/assign', body, {headers: this.userService.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }

    grant(userId: number, roleName: string, enable: string = 'true'): Observable<boolean> {
        let body = new URLSearchParams();
        body.append('userId', userId.toString());
        body.append('roleName', roleName);
        body.append('set', enable);
        return this.http
            .post(this.serviceUrl + '/grantRole', body, {headers: this.userService.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }
    
    changeActivity(userId: number, active: string = 'false') : Observable<boolean> {
        let body = new URLSearchParams();
        body.append('userId', userId.toString());
        body.append('active', active);
        return this.http
            .post(this.serviceUrl + '/ban', body, {headers: this.userService.getHeaders({produces: 'application/x-www-form-urlencoded'})})
            .map(r => r.ok);
    }
    
    loadActivity(offset: number, count: number): Observable<Array<string>> {
        return this.http
            .get(this.serviceUrl + '/activity', {headers: this.userService.getHeaders({consumes: 'application/json'}), params: {'from': offset, 'count': count}})
            .map(r => r.json() as string[]);
    }
}
