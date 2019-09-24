import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {RouterModule, Routes} from '@angular/router';
import {LocationStrategy, HashLocationStrategy} from '@angular/common';
import {UserService} from './service/user/user.service';
import {QuackService} from './service/quack/quack.service';
import {AdministrationService} from './service/administration/administration.service';
import {GlobalService} from './service/global/global.service';
import {AppComponent} from './app.component';
import {LoginComponent} from './component/login/login.component';
import {HomeComponent} from './component/home/home.component';
import {AdminComponent} from './component/admin/admin.component';
import {ProfilComponent} from './component/profil/profil.component';
import {SearchComponent} from './component/search/search.component';
import {QuackListComponent} from './component/quack-list/quack-list.component';
import {QuackComponent} from './component/quack/quack.component';
import {AuthguardService} from './authguard.service';
import {AdminguardService} from './adminguard.service';
import {RegisterComponent} from './component/register/register.component';
import {SafeHtmlPipe} from './safe-html.pipe';

const APP_ROUTES: Routes = [
    {
        path: 'home', component: HomeComponent, canActivate: [AuthguardService], data: {title: 'QuickQuack'}, children: [
            {path: 'chronik', component: QuackListComponent, data: {title: 'Chronik'}},
            {path: 'admin', component: AdminComponent, data: {title: 'Administration'}, canActivate: [AdminguardService]},
            {path: 'profil', component: ProfilComponent, data: {title: 'Profil'}},
            {path: 'search', component: SearchComponent}
        ]
    },
    {path: 'login', component: LoginComponent, data: {title: 'QuickQuack - Login'}},
    {path: 'register', component: RegisterComponent, data: {title: 'QuickQuack - Registrieren'}},
    {path: '', redirectTo: '/home/chronik', pathMatch: 'full'},
    {path: '**', redirectTo: '/home/chronik'}
];

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        HomeComponent,
        AdminComponent,
        ProfilComponent,
        SearchComponent,
        QuackComponent,
        QuackListComponent,
        RegisterComponent,
        SafeHtmlPipe
    ],
    imports: [
        RouterModule.forRoot(APP_ROUTES),
        BrowserModule,
        FormsModule,
        HttpModule
    ],
    providers: [{provide: LocationStrategy, useClass: HashLocationStrategy},
        GlobalService,
        AuthguardService,
        AdminguardService,
        UserService,
        QuackService,
        AdministrationService],
    bootstrap: [AppComponent]
})
export class AppModule {}
