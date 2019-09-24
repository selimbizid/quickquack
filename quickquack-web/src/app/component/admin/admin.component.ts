import {Component, OnInit} from '@angular/core';
import {AdministrationService} from '../../service/administration/administration.service';
import {UserService} from '../../service/user/user.service';
import {User} from '../../model/user';
import {Role} from '../../model/role';
import {Router} from '@angular/router';

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css'],
    providers: [AdministrationService]
})
export class AdminComponent implements OnInit {
    list: string[] = [];
    userList: User[] = [];
    roleList: Role[] = [];
    manageRoles: boolean = false;
    blockCandidate: User;
    roleCandidate: User;
    mode: number = 1;

    constructor(private aService: AdministrationService, private uService: UserService, private router: Router) {
        this.aService.getRoles().subscribe(roles => this.roleList = roles);
        let perms = JSON.parse(localStorage.getItem("permissions")) as string[];
        if (perms) {
            this.manageRoles = perms.filter(p => p == 'MANAGE_ROLES').length > 0;
        }
    }

    ngOnInit() {
        this.loadActivity(true);
    }

    loadActivity(fromStart: boolean = false) {
        this.mode = 1;
        if (fromStart) {
            this.list = [];
        }
        this.aService.loadActivity(this.list.length, 30).subscribe(ls => {
            this.list = this.list.concat(ls);
        });
    }
    
    setBlockCandidate(user: User) {
        this.blockCandidate = user;
    }
    
    setRoleCandidate(user: User) {
        this.roleCandidate = user;
    }
    
    loadUsers() {
        this.mode = 2;
        this.uService.findAll()
            .subscribe(ls => this.userList = ls);
    }
    
    loadBlockedUsers() {
        this.mode = 3;
    }
    
    visitProfile(id: number) {
        this.router.navigate(['/home/profil'], {queryParams: {'id':id}});
    }
    
    blockOrDeblock() {
        let future = !this.blockCandidate.active;
        this.aService.changeActivity(this.blockCandidate.id, future ? 'true' : 'false')
            .subscribe(b => {
                if (b) {
                    this.blockCandidate.active = future;
                }
        });
    }
    
    updateRole(roleName: string, add: boolean) {
        if (this.roleCandidate) {
            this.aService.grant(this.roleCandidate.id, roleName, add ? 'true' : 'false').subscribe(r => {
                if (add) {
                    this.roleCandidate.roles = this.roleCandidate.roles.concat(roleName);
                } else {
                    this.roleCandidate.roles = this.roleCandidate.roles.filter(r => r != roleName);
                }
            });
        }
    }
    
    canBeLocked(user: User): boolean {
        return user.roles.filter(r => r == 'SuperAdmin').length == 0;
    }
    
    validList(): boolean {
        return this.mode == 1 && this.list && this.list.length > 0;
    }
    
    validUserList(): boolean {
        return this.mode == 2 && this.list && this.userList.length > 0;
    }
    
    trackList(index: number, value: string) {
        return index;
    }
}
