import {Component} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {Router, RouterState, ActivatedRoute, NavigationEnd, Data} from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    constructor(titleService: Title, router: Router, activatedRoute: ActivatedRoute) {
        router.events.subscribe(event => {
            if (event instanceof NavigationEnd) {
                var title = this.getTitle(router.routerState, router.routerState.root).join(' - ');
                titleService.setTitle(title);
            }
        });
    }

    getTitle(state: RouterState, parent: ActivatedRoute): string[] {
        var data = [];
        if (parent && parent.snapshot.data && parent.snapshot.data['title']) {
            data.push(parent.snapshot.data['title']);
        }

        if (state && parent) {
            data.push(... this.getTitle(state, parent.firstChild));
        }
        return data;
    }
}
