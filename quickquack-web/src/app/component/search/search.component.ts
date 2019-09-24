import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {UserService} from '../../service/user/user.service';
import {QuackService} from '../../service/quack/quack.service';
import {User} from '../../model/user';
import {Quack} from '../../model/quack';

@Component({
    selector: 'search',
    templateUrl: './search.component.html',
    styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {
    DEFAULT_MODE: number = 0;
    USERS_MODE: number = 1;
    HASHTAG_MODE: number = 2;
    users: User[] = [];
    quacks: Quack[] = [];
    searchValue: string;
    noResults: boolean = false;
    furtherUsers: boolean = true;
    furtherQuacks: boolean = true;
    paramsSub: Subscription;
    
    @Input() key: string;
    mode: number;

    constructor(private uService: UserService, private qService: QuackService, private active: ActivatedRoute) {}

    ngOnInit() {
        this.paramsSub = this.active.queryParams.subscribe(params => {
            if (params['username']) {
                this.mode = this.USERS_MODE;
                this.searchValue = params['username'];
                this.load(true);
            } else if (params['hashtag']) {
                this.mode = this.HASHTAG_MODE;
                this.searchValue = params['hashtag'];
                this.load(true);
            } else {
                this.mode = this.DEFAULT_MODE;
                this.searchValue = "";
            }
        });
    }
    
    ngOnDestroy() {
        this.paramsSub.unsubscribe();
    }
    
    load(first: boolean = false) {
        switch (this.mode) {
            case this.USERS_MODE:
                if (first) {
                    this.users = [];
                }
                this.uService.find(this.searchValue, this.users.length)
                    .subscribe(ls => {
                        if (first) {
                            this.noResults = !ls || ls.length == 0;
                        }
                        this.furtherUsers = ls.length == 10;
                        this.users = this.users.concat(ls);
                });
                break;
            case this.HASHTAG_MODE:
                if (first) {
                    this.quacks = [];
                }
                this.qService.findByHashtag(this.searchValue, this.quacks.length)
                    .subscribe(ls => {
                        if (first) {
                            this.noResults = !ls || ls.length == 0;
                        }
                        this.furtherQuacks = ls.length == 10;
                        this.quacks = this.quacks.concat(ls);
                    });
                break;
        }
    }
    
    shouldLoad(): boolean {
        return (this.mode == 1 && this.furtherUsers) || (this.mode == 2 && this.furtherQuacks);
    }
    
    trackUserById(index: number, user: User) {
        return user.id;
    }
    
    trackQuackById(index: number, quack: Quack) {
        return quack.id;
    }
}
