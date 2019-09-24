import {Component, OnInit} from '@angular/core';
import {Response} from '@angular/http';
import {ActivatedRoute} from '@angular/router';
import {UserService} from '../../service/user/user.service';
import {AdministrationService} from '../../service/administration/administration.service';
import {GlobalService} from '../../service/global/global.service';
import {User} from '../../model/user';

@Component({
    selector: 'profil',
    templateUrl: './profil.component.html',
    styleUrls: ['./profil.component.css'],
    providers: [UserService]
})
export class ProfilComponent implements OnInit {
    u: User;
    passChangeErrorFlags: number = 0;
    self: boolean = true;
    banOrDeban: string;
    canBan: boolean = false;
    usernameChanged: boolean;
    passwordChanged: boolean;
    followOrUnfollow: string;
    blockOrDeblock: string;
    usernameChangeInfo: string;
    passwordChangeInfo: string;

    constructor(private service: UserService, private aService: AdministrationService, private active: ActivatedRoute, private gService: GlobalService) {}

    ngOnInit() {
        this.active.queryParams.subscribe(params => {
            if (params['id']) {
                this.service.getById(params['id'] as number).subscribe(u => {
                    this.u = u;
                    this.updateData();
                });
            } else {
                this.service.getById(+localStorage.getItem("id")).subscribe(u => {
                    this.u = u;
                    this.updateData();
                });
            }
        });
    }
    
    updateData() {
        this.self = (+localStorage.getItem("id")) == this.u.id;
        if (!this.self) {
            this.setFollowButton();
            this.setBlockButton();
            let pems = JSON.parse(localStorage.getItem("permissions")) as string[];
            this.canBan = pems.filter(p => p == 'MANAGE_USERS').length == 1;
            this.setBanButton()
        }
    }

    private setBanButton() {
        if (this.u.active) {
            this.banOrDeban = "Konto sperren";
        } else this.banOrDeban = "Konto entsperren";
    }

    private setFollowButton() {
        if (this.u.followedFromSelf) {
            this.followOrUnfollow = "Nicht mehr folgen";
        } else this.followOrUnfollow = "Folgen";
    }

    private setBlockButton() {
        if (this.u.blockedFromSelf) {
            this.blockOrDeblock = "Deblokieren";
        } else this.blockOrDeblock = "Blokieren";
    }

    block() {
        let future = !this.u.blockedFromSelf;
        this.service.block(this.u.id.toString(), future ? 'true' : 'false')
            .subscribe(s => {
                this.u.blockedFromSelf = future;
                this.setBlockButton();
                this.gService.quackListShouldReload.next();
            });
    }

    follow() {
        let future = !this.u.followedFromSelf;
        this.service.toggleFollow(this.u.id)
            .subscribe(s => {
                this.u.followedFromSelf = future;
                this.setFollowButton();
                this.gService.quackListShouldReload.next();
            });
    }

    ban() {
        let future = !this.u.active;
        this.aService.changeActivity(this.u.id, future ? 'true' : 'false').subscribe(s => {
            this.u.active = future;
            this.setBanButton();
        });
    }

    changeAlias(newName: string) {
        if (newName) {
            this.service.changeAlias(newName).subscribe(s => {
                this.u.alias = newName;
                this.usernameChangeInfo = "Benutzername erfolgreich geändert!";
                this.usernameChanged = true;
                this.gService.aliasChanged.next(newName);
            }, f => {
                this.usernameChanged = false;
                if (f instanceof Response) {
                    this.usernameChangeInfo = (f as Response).text();
                }
            }
            );
        }
    }

    changePassword(op: string, np: string, npc: string) {
        this.passChangeErrorFlags = 0;
        this.passwordChangeInfo = null;
        if (!op || op.length == 0) {
            this.passChangeErrorFlags = 1;
        }
        if (!np || np.length == 0) {
            this.passChangeErrorFlags = this.passChangeErrorFlags | 2;
        }
        if (!npc || npc.length == 0 || (np && np != npc)) {
            this.passChangeErrorFlags = this.passChangeErrorFlags | 4;
        }
        if (this.passChangeErrorFlags > 0) {
            return;
        }
        this.service.changePassword(op, np).subscribe(s => {
            this.passwordChangeInfo = "Passwort erfolgreich geändert.";
            this.passwordChanged = true;
        }, f => {
            this.passwordChanged = false;
            if (f instanceof Response) {
                this.passwordChangeInfo = (f as Response).text();
            }
        });
    }
    
    checkFlag(nr: number): boolean {
        return (this.passChangeErrorFlags & nr) != 0;
    }
}
