import {Component, OnInit, Input, AfterViewInit} from '@angular/core';
import {Quack} from '../../model/quack';
import {Comment} from '../../model/comment';
import {QuackService} from '../../service/quack/quack.service';
import {UserService} from '../../service/user/user.service';
import {GlobalService} from '../../service/global/global.service';
import {AdministrationService} from '../../service/administration/administration.service';
import {Router} from '@angular/router';

@Component({
    selector: 'quack',
    templateUrl: './quack.component.html',
    styleUrls: ['./quack.component.css'],
    providers: [QuackService, UserService, AdministrationService]
})
export class QuackComponent implements OnInit, AfterViewInit {
    @Input() quack: Quack;
    tags: string[] = [];
    likeOrDislike: string;
    followOrUnfollow: string;
    canDelete: boolean;
    canBan: boolean;
    self: boolean;
    processedContent: string;
    plainContent: string = "";
    editMode: boolean;
    editText: string = "Editieren";
    edited: boolean = false;
    commentsShown: boolean = false;
    commentContent: string = "";
    commentError: boolean = false;
    selectedScope: string;
    currentId: number;

    constructor(private service: QuackService, private uService: UserService, private aService: AdministrationService, private gService: GlobalService, private router: Router) {}

    ngOnInit() {
        this.plainContent = this.quack.content;
        this.processContent();
        this.updateLikeText();
        this.updateFollowText();
        this.currentId = +localStorage.getItem("id");
        this.self = this.currentId == this.quack.authorId;
        this.selectedScope = this.getScopeName(this.quack.scope);
        let perms = JSON.parse(localStorage.getItem("permissions")) as string[];
        if (perms) {
            this.canDelete = this.self || perms.filter(p => p == "MANAGE_QUACKS").length == 1;
            this.canBan = perms.filter(p => p == "MANAGE_USERS").length == 1;
        }
        if (!this.quack.comments) {
            this.quack.comments = [];
        }
    }

    ngAfterViewInit() {
        this.processHashtags();
    }

    loadComments() {
        if (this.quack.comments.length < this.quack.commentsCount) {
            this.service
                .getComments(this.quack.id, this.quack.comments.length)
                .subscribe(comms => this.quack.comments = this.quack.comments.concat(comms));
        }
    }

    postComment() {
        if (this.commentContent.length > 0) {
            this.service.putComment(new Comment({quackId: this.quack.id, content: this.commentContent}))
                .subscribe(s => {
                    if (s) {
                        this.commentError = false;
                        this.quack.comments.unshift(s);
                        this.quack.commentsCount++;
                        this.commentContent = "";
                    }
                }, f => {
                    this.commentError = true;
                });
        } else this.commentError = true;
    }
    
    deleteComment(id: number) {
        this.service.removeComment(id).subscribe(s => {
            if (s) {
                this.quack.comments = this.quack.comments.filter(c => c.id != id);
                this.quack.commentsCount--;
            }
        });
    }

    commentsButtonClicked() {
        this.commentsShown = !this.commentsShown;
        if (this.commentsShown) {
            this.service.findById(this.quack.id).subscribe(q => {
                this.quack = q;
            });
        } else this.quack.comments = [];
    }

    ban() {
        this.aService.changeActivity(this.quack.authorId).subscribe(s => {
            this.gService.userBanned.next(this.quack.authorId);
        });
    }

    block() {
        this.uService.block(this.quack.authorId.toString()).subscribe(s => {
            this.gService.userBlocked.next(this.quack.authorId);
        });
    }

    edit() {
        if (this.editMode = !this.editMode) {
            this.editText = "Speichern";
        } else {
            this.quack.content = this.plainContent;
            this.editText = "Editieren"
            this.processContent();
            this.service.edit(this.quack).subscribe(s => this.edited = true, f => console.log("Quack konnte nicht aktualisiert werden!"));
        }
    }

    delete() {
        this.service.remove(this.quack.id).subscribe(s => {
            this.gService.quackDeleted.next(this.quack.id);
        });
    }

    toggleLike() {
        let future = !this.quack.liked;
        this.service.like(this.quack.id, future).subscribe(s => {
            this.quack.liked = future;
            if (future) {
                this.quack.totalLikes++;
            } else this.quack.totalLikes--;
            this.updateLikeText()
        });
    }

    private updateLikeText() {
        this.likeOrDislike = this.quack.liked ? "Gefällt mir nicht mehr" : "Gefällt mir";
    }
    
    toggleFollow() {
        this.uService.toggleFollow(this.quack.authorId).subscribe(s => {
            this.gService.quackListShouldReload.next();
        });
    }

    private updateFollowText() {
        this.followOrUnfollow = this.quack.authorFollowed ? "Nicht mehr folgen" : "Folgen";
    }

    trackCommentById(index: number, comment: Comment) {
        return comment.id;
    }
    
    ownComment(id: number): boolean {
        return this.currentId == id;
    }

    private processContent() {
        let inTag = false;
        let curChar;
        let curTag: string = "";
        this.plainContent = this.plainContent + " "; // Parse eventually lastest hashtag
        this.processedContent = "";
        this.tags = [];
        for (var i = 0; i < this.plainContent.length; i++) {
            curChar = this.plainContent.charAt(i);
            if (inTag) {
                if (curChar == ' ' || curChar == '\n') {
                    this.processedContent = this.processedContent.concat("</a>");
                    this.tags.push(curTag);
                    inTag = false;
                    curTag = "";
                } else curTag = curTag.concat(curChar);
            } else if (curChar == "#") {
                let tagName = 'tag-' + this.quack.id + '-' + this.tags.length;
                this.processedContent = this.processedContent.concat('<a role="button" id="' + tagName + '">');
                inTag = true;
            }
            this.processedContent = this.processedContent.concat(curChar);
        }
        this.plainContent = this.plainContent.trim();
        this.processedContent = this.processedContent.trim();
    }

    private processHashtags() {
        for (var i = 0; i < this.tags.length; i++) {
            const z = i;
            document.querySelector('#tag-' + this.quack.id + '-' + i).addEventListener('click', e => this.findHashtag(z));
        }
    }
    
    updateScope(event) {
        let future = this.getScopeValue(this.selectedScope);
        this.service.changeScope(this.quack.id, future)
            .subscribe(ok => {
                this.quack.scope = future;
        });
    }
    
    private getScopeValue(scope: string): number {
        switch (scope) {
            case 'Follower':
                return 1;
            case 'Privat':
                return 0
            default:
                return 2;
        }
    }
    
    private getScopeName(scope: number): string {
        switch (scope) {
            case 1:
                return 'Follower';
            case 2:
                return 'Alle'
            default:
                return 'Privat';
        }
    }

    findHashtag(idx: number) {
        this.router.navigate(['/home/search'], {queryParams: {'hashtag': this.tags[idx]}});
    }
}
