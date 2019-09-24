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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.DateTime;

/**
 * @author selimbizid
 */
@Entity
@Table(name = "quack")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Quack.findAll", query = "SELECT q FROM Quack q ORDER BY q.postDate DESC")
    , @NamedQuery(name = "Quack.findById", query = "SELECT q FROM Quack q WHERE q.id = :id ORDER BY q.postDate DESC")
    , @NamedQuery(name = "Quack.findByContent", query = "SELECT q FROM Quack q WHERE q.content LIKE :content ORDER BY q.postDate DESC")})
public class Quack implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "post_date")
    private DateTime postDate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "content")
    private String content;
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User author;
    @OrderBy("postDate DESC")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quack")
    private List<Comment> comments;
    @JoinTable(
            name="quacklike",
            joinColumns=@JoinColumn(name="quack_id", referencedColumnName="id"),
            inverseJoinColumns=@JoinColumn(name="user_id", referencedColumnName="id")
    )
    @ManyToMany
    private List<User> likers;
    @JoinTable(
            name = "quacktag",
            joinColumns = @JoinColumn(name = "quack_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_identifier", referencedColumnName = "identifier")
    )
    @ManyToMany
    private List<Hashtag> hashTags;
    @Basic(optional = false)
    @NotNull
    @Column(name = "scope")
    private short scope;

    public Quack() {
    }

    public Quack(Integer id) {
        this.id = id;
    }

    public Quack(Integer id, DateTime postDate, String content) {
        this.id = id;
        this.postDate = postDate;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DateTime getPostDate() {
        return postDate;
    }

    public void setPostDate(DateTime postDate) {
        this.postDate = postDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @XmlTransient
    public List<User> getLikers() {
        return likers;
    }

    public void setLikers(List<User> likers) {
        this.likers = likers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @XmlTransient
    public List<Hashtag> getHashTags() {
        return hashTags;
    }

    public void setHashTags(List<Hashtag> hashTags) {
        this.hashTags = hashTags;
    }
    
    public short getScope() {
        return scope;
    }

    public void setScope(short scope) {
        this.scope = scope;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Quack)) {
            return false;
        }
        Quack other = (Quack) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.tudortmund.webtech2.quickquack.ejb.entity.Quack[ id=" + id + " ]";
    }

}
