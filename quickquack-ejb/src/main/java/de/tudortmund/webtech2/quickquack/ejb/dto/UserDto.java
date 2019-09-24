package de.tudortmund.webtech2.quickquack.ejb.dto;

import java.util.Collection;

public class UserDto {
    private int id;
    private String email;
    private String alias;
    private String password;
    private long postsCount;
    private long following;
    private long followed;
    private boolean followedFromSelf;
    private boolean blockedFromSelf;
    private String lastestActivity;
    private String authToken;
    private Collection<String> permissions;
    private Collection<String> roles;
    private boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(long postsCount) {
        this.postsCount = postsCount;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getFollowed() {
        return followed;
    }

    public void setFollowed(long followed) {
        this.followed = followed;
    }

    public boolean isFollowedFromSelf() {
        return followedFromSelf;
    }

    public void setFollowedFromSelf(boolean followedFromSelf) {
        this.followedFromSelf = followedFromSelf;
    }

    public boolean isBlockedFromSelf() {
        return blockedFromSelf;
    }

    public void setBlockedFromSelf(boolean blockedFromSelf) {
        this.blockedFromSelf = blockedFromSelf;
    }
    
    public String getLastestActivity() {
        return lastestActivity;
    }

    public void setLastestActivity(String lastestActivity) {
        this.lastestActivity = lastestActivity;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Collection<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions = permissions;
    }
    
    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
    
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
