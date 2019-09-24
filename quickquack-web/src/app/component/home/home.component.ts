import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, RoutesRecognized, ActivatedRoute} from '@angular/router';
import {UserService} from '../../service/user/user.service';
import {QuackService} from '../../service/quack/quack.service';
import {GlobalService} from '../../service/global/global.service';
import {Subscription} from 'rxjs/Subscription';
import {Response} from '@angular/http';
import {Quack} from '../../model/quack';

@Component({
    selector: 'home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    providers: [UserService, QuackService]
})
export class HomeComponent implements OnInit, OnDestroy {
    isAdmin: boolean;
    searchError: boolean;
    alias: string;
    currentComponent: string = "/chronik";
    quackContent: string;
    quackScope: string = "Alle";
    searchValue: string;
    welcome: boolean;
    aliasUpdateError: string;
    aliasUpdated: boolean = false;
    aliasChangeSubscription: Subscription;
    constructor(private uService: UserService, private qService: QuackService, private router: Router, private gService: GlobalService, private active: ActivatedRoute) {}

    ngOnInit() {
        this.alias = localStorage.getItem("currentAlias");
        let perms = JSON.parse(localStorage.getItem("permissions")) as string[];
        if (perms) {
            this.isAdmin = perms.filter(perm => perm == "MANAGE_ROLES" || perm == "MANAGE_USERS").length > 0;
        }
        this.router.events.subscribe(v => {
            if (v instanceof RoutesRecognized && v.url && v.url.length > 0) {
                this.currentComponent = v.url.substring(v.url.lastIndexOf('/'));
            }
        });
        this.active.queryParams.subscribe(params => {
            this.welcome = params['new'];
        });
        this.aliasChangeSubscription = this.gService.aliasChanged.asObservable().subscribe(a => this.alias = a);
    }
    
    ngOnDestroy() {
        this.aliasChangeSubscription.unsubscribe();
    }

    search() {
        if (this.searchValue && this.searchValue.length > 0) {
            if (this.searchValue == '@' || this.searchValue == '.') {
                this.searchError = true;
                return;
            }
            this.searchError = false;
            if (this.searchValue.startsWith('#')) {
                this.router.navigate(['/home/search'], {queryParams: {'hashtag': this.searchValue.substring(1)}});
            } else {
                this.router.navigate(['/home/search'], {queryParams: {'username': this.searchValue}});
            }
        } else this.searchError = true;
        this.searchValue = "";
    }

    setChronik() {
        this.currentComponent = "/chronik";
    }

    setProfil() {
        this.currentComponent = "/profil";
    }

    setAdmin() {
        this.currentComponent = "/admin";
    }
    
    postQuack() {
        this.qService.put(new Quack({content: this.quackContent, scope: this.getScope(this.quackScope), authorId: localStorage.getItem("id")}))
            .subscribe(s => {
                this.quackContent = "";
                this.gService.quackPosted.next(s);
            },
            f => alert("Fehler!"));
    }
    
    updateAlias(alias: string) {
        if (alias && alias.length > 0) {
            this.uService.changeAlias(alias).subscribe(succ => {
                this.gService.aliasChanged.next(alias);
                this.aliasUpdated = true;
                this.welcome = false;
            }, f => {
                if (f instanceof Response) {
                    this.aliasUpdateError = (f as Response).text();
                }
            });
        }
    }

    private getScope(scope: string): number {
        switch (scope) {
            case 'Follower':
                return 1;
            case 'Privat':
                return 0
            default:
                return 2;
        }
    }
}
