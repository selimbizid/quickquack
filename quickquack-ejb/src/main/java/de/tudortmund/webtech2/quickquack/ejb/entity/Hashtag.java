package de.tudortmund.webtech2.quickquack.ejb.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "hashtag")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Hashtag.findAll", query = "SELECT h FROM Hashtag h")
    , @NamedQuery(name = "Hashtag.findByIdentifier", query = "SELECT h FROM Hashtag h WHERE h.identifier = :identifier")})
public class Hashtag implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    @Column(name = "identifier")
    private String identifier;
    @JoinTable(
            name = "quacktag",
            joinColumns = @JoinColumn(name = "tag_identifier", referencedColumnName = "identifier"),
            inverseJoinColumns = @JoinColumn(name = "quack_id", referencedColumnName = "id")
    )
    @OrderBy("postDate DESC")
    @ManyToMany
    private List<Quack> relatedQuacks;

    public Hashtag() {
    }

    public Hashtag(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<Quack> getRelatedQuacks() {
        return relatedQuacks;
    }

    public void setRelatedQuacks(List<Quack> relatedQuacks) {
        this.relatedQuacks = relatedQuacks;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (identifier != null ? identifier.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hashtag)) {
            return false;
        }
        Hashtag other = (Hashtag) object;
        if ((this.identifier == null && other.identifier != null) || (this.identifier != null && !this.identifier.equals(other.identifier))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.tudortmund.webtech2.quickquack.ejb.entity.Hashtag[ identifier=" + identifier + " ]";
    }

}
