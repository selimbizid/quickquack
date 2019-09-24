import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';

@Injectable()
export class AdminguardService implements CanActivate {

    constructor(private router: Router) {}

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        let raw = localStorage.getItem('permissions');
        if (raw) {
            let perms = JSON.parse(raw) as string[];
            if (perms.filter(perm => perm == "MANAGE_ROLES" || perm == "MANAGE_USERS").length > 0) {
                return true;
            }
        }
        this.router.navigate(['/home']);
        return false;
    }
}
