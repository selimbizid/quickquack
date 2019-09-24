import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import {Quack} from '../../model/quack';
import {QuackService} from '../../service/quack/quack.service';
import {GlobalService} from '../../service/global/global.service';
import {Subscription} from 'rxjs/Subscription';
import {Observable} from 'rxjs/Rx';

@Component({
    selector: 'quack-list',
    templateUrl: './quack-list.component.html',
    styleUrls: ['./quack-list.component.css'],
    providers: [QuackService]
})
export class QuackListComponent implements OnInit, OnDestroy {
    quacks: Quack[] = [];
    shouldLoad: boolean = true;
    subscriptions: Array<Subscription> = [];
    @Input() id: number = -1;

    constructor(private qService: QuackService, private gService: GlobalService) {}

    ngOnInit() {
        this.loadQuacks();
        this.subscriptions.push(this.gService.quackListShouldReload.asObservable().subscribe(e => {
            this.quacks = [];
            this.loadQuacks();
        }));
        this.subscriptions.push(this.gService.quackPosted.asObservable().subscribe(q => {
            q.authorActive = true;
            this.quacks = [q].concat(this.quacks);
        }));
        this.subscriptions.push(this.gService.quackDeleted.asObservable().subscribe(id => {
            this.quacks = this.quacks.filter(q => q.id != id);
        }));
        this.subscriptions.push(this.gService.userBlocked.asObservable().subscribe(id => {
            this.quacks = this.quacks.filter(q => q.authorId != id);
        }));
        this.subscriptions.push(this.gService.userBanned.asObservable().subscribe(id => {
            this.quacks.forEach(q => {
                if (q.authorId == id) {
                    q.authorActive = false;
                }
            });
        }));
        if (this.id != (+localStorage.getItem("id"))) {
            this.subscriptions.push(Observable.timer(8000, 8000).subscribe(t => {
                this.refreshQuacks();
            }));
        }
    }

    ngOnDestroy() {
        this.subscriptions.forEach(s => s.unsubscribe());
    }

    private refreshQuacks() {
        if (this.id >= 0) {
            this.qService.findByUserId(this.id, 0, this.quacks.length > 0 ? this.quacks[0].id : 0)
                .subscribe(qs => {
                    qs.reverse().forEach(q => this.quacks.unshift(q));
                },
                fl => this.shouldLoad = false);
        } else this.qService.get(0, this.quacks.length > 0 ? this.quacks[0].id : 0)
            .subscribe(qs => {
                qs.reverse().forEach(q => this.quacks.unshift(q));
            },
            fl => this.shouldLoad = false);
    }

    loadQuacks() {
        if (this.id >= 0) {
            this.qService.findByUserId(this.id, this.quacks.length)
                .subscribe(qs => {
                    this.quacks = this.quacks.concat(qs);
                    this.shouldLoad = qs.length == 10;
                },
                fl => this.shouldLoad = false);
        } else this.qService.get(this.quacks.length)
            .subscribe(qs => {
                this.quacks = this.quacks.concat(qs);
                this.shouldLoad = qs.length == 10;
            },
            fl => this.shouldLoad = false);
    }

    trackById(index: number, quack: Quack) {
        return quack.id;
    }
}
