import {Component, OnInit} from '@angular/core';
import {UserService} from '../../service/user/user.service';
import {Response} from '@angular/http';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    providers: [UserService]
})
export class LoginComponent implements OnInit {
    email: string;
    invalidEmail: boolean;
    newUser: boolean;
    password: string;
    invalidPassword: boolean;
    error: string;

    constructor(private service: UserService, private router: Router, private active: ActivatedRoute) {}

    ngOnInit() {
        localStorage.clear();
        this.active.queryParams.subscribe(params => {
            this.newUser = params['new'];
        });
    }

    onSubmit() {
        this.invalidEmail = !this.email || this.email.length == 0;
        this.invalidPassword = !this.password || this.password.length == 0;

        if (this.invalidEmail || this.invalidPassword) {
            return;
        }

        this.service.login(this.email, this.password)
            .subscribe(u => {
                localStorage.setItem("id", u.id.toString());
                localStorage.setItem("email", u.email);
                localStorage.setItem('authToken', u.authToken);
                localStorage.setItem('currentAlias', u.alias);
                localStorage.setItem('permissions', JSON.stringify(u.permissions));
                this.router.navigate(['/home/chronik'], this.newUser ? {queryParams: {'new': true}} : {});
            },
                f => {
                if (f instanceof Response) {
                    this.error = (f as Response).text();
                } else this.error = "Login fehlgeschlagen"
            });
    }
}
