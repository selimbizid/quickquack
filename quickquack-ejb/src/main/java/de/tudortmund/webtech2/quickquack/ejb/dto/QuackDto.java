package de.tudortmund.webtech2.quickquack.ejb.dto;

import java.util.Collection;

public class QuackDto {
    private int id;
    private String authorName;
    private int authorId;
    private boolean authorFollowed;
    private boolean authorActive;
    private boolean authorBlocked;
    private String content;
    private String postDate;
    private long totalLikes;
    private Collection<CommentDto> comments;
    private int commentsCount;
    private boolean liked;
    private short scope;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public boolean isAuthorFollowed() {
        return authorFollowed;
    }

    public void setAuthorFollowed(boolean authorFollowed) {
        this.authorFollowed = authorFollowed;
    }

    public boolean isAuthorActive() {
        return authorActive;
    }

    public void setAuthorActive(boolean authorActive) {
        this.authorActive = authorActive;
    }

    public boolean isAuthorBlocked() {
        return authorBlocked;
    }

    public void setAuthorBlocked(boolean authorBlocked) {
        this.authorBlocked = authorBlocked;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Collection<CommentDto> getComments() {
        return comments;
    }

    public void setComments(Collection<CommentDto> comments) {
        this.comments = comments;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }
    
    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }
    
    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public short getScope() {
        return scope;
    }

    public void setScope(short scope) {
        this.scope = scope;
    }
}
