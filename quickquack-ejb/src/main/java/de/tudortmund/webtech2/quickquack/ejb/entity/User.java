package de.tudortmund.webtech2.quickquack.ejb.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.DateTime;

/**
 * @author selimbizid
 */
@Entity
@Table(name = "user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
    , @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id")
    , @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email")
    , @NamedQuery(name = "User.findByAlias", query = "SELECT u FROM User u WHERE u.alias = :alias")
    , @NamedQuery(name = "User.findByAccountActive", query = "SELECT u FROM User u WHERE u.accountActive = :accountActive")})
public class User implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    private List<Comment> commentList;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "email", unique = true)
    private String email;
    @Size(max = 45)
    @Column(name = "alias", unique = true)
    private String alias;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "password")
    private String password;
    @Size(max = 45)
    @Column(name = "password_salt")
    private String passwordSalt;
    @Column(name = "lastest_activity")
    //@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastestActivity;
    @Basic(optional = false)
    @NotNull
    @Column(name = "account_active")
    private short accountActive;
    @JoinTable(
            name = "userrole",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @ManyToMany
    private List<Role> roles;
    @JoinTable(
            name = "quacklike",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "quack_id", referencedColumnName = "id")
    )
    @OrderBy("postDate DESC")
    @ManyToMany
    private List<Quack> likedQuacks;
    @OrderBy("postDate DESC")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    private List<Quack> quacks;
    @JoinTable(
            name = "follow",
            joinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id", referencedColumnName = "id")
    )
    @ManyToMany
    private List<User> followedUsers;
    @JoinTable(
            name = "follow",
            joinColumns = @JoinColumn(name = "followed_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id")
    )
    @ManyToMany
    private List<User> followingUsers;
    @JoinTable(
            name = "block",
            joinColumns = @JoinColumn(name = "blocker_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_id", referencedColumnName = "id")
    )
    @ManyToMany
    private List<User> blockedUsers;

    public User() {
    }

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, String email, String password, short accountActive) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.accountActive = accountActive;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public DateTime getLastestActivity() {
        return lastestActivity;
    }

    public void setLastestActivity(DateTime lastestActivity) {
        this.lastestActivity = lastestActivity;
    }

    public short getAccountActive() {
        return accountActive;
    }

    public void setAccountActive(short accountActive) {
        this.accountActive = accountActive;
    }

    @XmlTransient
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @XmlTransient
    public List<Quack> getLikedQuacks() {
        return likedQuacks;
    }

    public void setLikedQuacks(List<Quack> likes) {
        this.likedQuacks = likes;
    }

    @XmlTransient
    public List<Quack> getQuacks() {
        return quacks;
    }

    public void setQuacks(List<Quack> quacks) {
        this.quacks = quacks;
    }
    
    @XmlTransient
    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @XmlTransient
    public List<User> getFollowedUsers() {
        return followedUsers;
    }

    public void setFollowedUsers(List<User> followedUsers) {
        this.followedUsers = followedUsers;
    }

    @XmlTransient
    public List<User> getFollowingUsers() {
        return followingUsers;
    }

    public void setFollowingUsers(List<User> followingUsers) {
        this.followingUsers = followingUsers;
    }

    public List<User> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(List<User> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.tudortmund.webtech2.quickquack.ejb.entity.User[ id=" + id + " ]";
    }

}
