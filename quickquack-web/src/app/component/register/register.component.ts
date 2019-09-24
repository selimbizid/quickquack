import {Component} from '@angular/core';
import {UserService} from '../../service/user/user.service';
import {Response} from '@angular/http';
import {Router} from '@angular/router';

@Component({
    selector: 'register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css'],
    providers: [UserService]
})
export class RegisterComponent {
    email: string;
    errorFlags: number = 0;
    invalidEmail: boolean = false;
    password: string;
    passwordConfirm: string;
    invalidPassword: boolean = false;
    error: string;

    constructor(private service: UserService, private router: Router) {}

    onSubmit() {
        this.errorFlags = 0;
        if (!this.email || this.email.length == 0) {
            this.errorFlags = 1;
        }
        if (!this.password || this.password.length == 0) {
            this.errorFlags = this.errorFlags | 2;
        }
        if (!this.passwordConfirm || this.passwordConfirm.length == 0 || (this.password && this.password != this.passwordConfirm)) {
            this.errorFlags = this.errorFlags | 4;
        }
        if (this.errorFlags > 0) {
            return;
        }

        this.service.register(this.email, this.password)
            .subscribe(succ => {
                if (succ) {
                    this.router.navigate(['/login'], {queryParams: {'new': true}});
                } else this.error = "Registrierung fehlgeschlagen";
            }, f => {
                if (f instanceof Response) {
                    this.error = (f as Response).text();
                } else this.error = "Registrierung fehlgeschlagen"
            });
    }
    
    checkFlag(flag: number): boolean {
        return (this.errorFlags & flag) > 0;
    }
}
