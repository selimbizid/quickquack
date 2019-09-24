package de.tudortmund.webtech2.quickquack.ejb.dao;

import de.tudortmund.webtech2.quickquack.ejb.entity.Comment;
import de.tudortmund.webtech2.quickquack.ejb.entity.Hashtag;
import de.tudortmund.webtech2.quickquack.ejb.entity.Quack;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.other.GlobalConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * Data access object that handles the quacks and it's comments. Provided by the
 * EJB container.
 */
@Dependent
public class QuackDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * Returns the list of all quacks available on the system, without scope
     * filtering, therefore should be used only by users with permission
     * MANAGE_QUACKS
     *
     * @param offset Start index, from which the quacks are loaded
     * @param count Number of quacks to be loaded
     * @param since Index of the quack, where the returned qzacks are newer than it
     * @return List of quacks meeting the specified criterias
     * @throws QuackDataAccessException If whatever error happens
     * @see QuackDao#getRecent(java.lang.String, int, int)
     */
    public List<Quack> findAll(int offset, int count, int since) throws QuackDataAccessException {
        try {
            return em.createQuery("SELECT q FROM Quack q WHERE q.id > :since ORDER BY q.postDate DESC", Quack.class)
                    .setParameter("since", since)
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Returns the quack with the specified ID, if the given user principal has
     * the right to see it.
     *
     * @param email Principal of the calling user. If the parameter is null, the
     * scope won't be considered
     * @param quackid ID of the quack
     * @return Quack with the specified ID if exists
     * @throws QuackDataAccessException If no quack with the specified ID exists
     * @see QuackDao#findAll(int, int)
     * @see QuackDao#findByHashtag(java.lang.String, java.lang.String, int, int)
     * @see QuackDao#findByUserId(int, int, int)
     */
    public Quack findById(String email, int quackid) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Quack> q = b.createQuery(Quack.class);
            Root<Quack> from = q.from(Quack.class);
            Predicate pred = b.equal(from.get("id"), quackid);
            if (email != null) {
                pred = b.and(pred, generateRestrictions(b, q, from, email));
            }
            return em.createQuery(q.where(pred)).getSingleResult();
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Quack.class, e.getCause());
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Finds the list of the quacks which contains the specified hash tag.
     *
     * @param email Current user principal
     * @param tag Hash tag to be looked for
     * @param offset Start offset of the returned list
     * @param limit Maximal count of quacks to be searched
     * @return List of the found quacks that met the criteria
     * @throws QuackDataAccessException If whatever error occurs
     * @see QuackDao#findAll(int, int)
     * @see QuackDao#findById(java.lang.String, int)
     * @see QuackDao#findByUserId(int, int, int)
     */
    public Collection<Quack> findByHashtag(String email, String tag, int offset, int limit) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Quack> q = b.createQuery(Quack.class);
            Root<Quack> from = q.from(Quack.class);
            Predicate pred = b.isMember(new Hashtag(tag), from.<Collection<Hashtag>>get("hashTags"));
            if (email != null) {
                pred = b.and(pred, generateRestrictions(b, q, from, email));
            }
            return em.createQuery(q.where(pred).orderBy(b.desc(from.get("postDate"))))
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Quack.class, e.getCause());
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Finds the list of quacks that the user of the given parameter has posted
     *
     * @param principal Principal of the current user
     * @param userId Author of the quacks
     * @param offset Start index of the returned list
     * @param limit Maximal count of quacks to be searched
     * @param since Index of the quack, where the returned qzacks are newer than it
     * @return List of quacks of the given user
     * @throws QuackDataAccessException If whatever error occurs
     * @see QuackDao#findAll(int, int)
     * @see QuackDao#findByHashtag(java.lang.String, java.lang.String, int, int)
     * @see QuackDao#findById(java.lang.String, int)
     */
    public Collection<Quack> findByUserId(String principal, int userId, int offset, int limit, int since) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Quack> q = b.createQuery(Quack.class);
            Root<Quack> from = q.from(Quack.class);
            q.select(from);
            Predicate pred = b.equal(from.get("author").get("id"), userId);
            if (principal != null) {
                pred = b.and(pred, generateRestrictions(b, q, from, principal));
            }
            return em.createQuery(q.where(pred, b.greaterThan(from.<Integer>get("id"), since)).orderBy(b.desc(from.get("postDate"))))
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Finds quacks whose content contains the given parameter
     *
     * @param regex Regex to be searched
     * @param offset Start offset of the returned list
     * @param limit Maximal count of quacks to be returned
     * @return List of the found quacks
     * @throws QuackDataAccessException
     * @deprecated Use findByHashtag instead
     */
    @Deprecated()
    public Collection<Quack> findByContent(String regex, int offset, int limit) throws QuackDataAccessException {
        try {
            TypedQuery<Quack> u = em.createNamedQuery("Quack.findByContent", Quack.class);
            u.setParameter("content", '%' + regex + '%');
            u.setFirstResult(offset);
            u.setMaxResults(limit);
            return u.getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Changes the scope of the given quack
     *
     * @param email Author of the quack
     * @param qid Quack ID
     * @param newScope New scope to be assigned (must be between 0 and 2)
     * @return true if the scope has been changed, otherwise false
     * @throws QuackDataAccessException If whatever error occurs
     */
    public boolean changeScope(String email, int qid, short newScope) throws QuackDataAccessException {
        try {
            Quack target = em.find(Quack.class, qid);
            if (target != null && target.getAuthor().getEmail().equals(email)) {
                target.setScope(newScope);
                protocol("Scope of quack #" + qid + " changed to " + newScope);
                return true;
            }
            return false;
        } catch (Throwable e) {
            throw getException(e);
        }
    }

    /**
     * Returns the count of the quacks that the user has posted
     *
     * @param userId ID of the user
     * @return Count of the quacks posted by that user
     * @throws QuackDataAccessException If whatever error occurs
     */
    public long getCountOfUser(int userId) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Long> q = b.createQuery(Long.class);
            Root<Quack> from = q.from(Quack.class);
            q.select(b.count(from));
            q.where(b.equal(from.get("author").get("id"), userId));
            return em.createQuery(q).getSingleResult();
        } catch (NoResultException e) {
            return 0;
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Posts a new quack. The author parameter must be always set.
     *
     * @param q Quack to be stored on the system
     * @return The same quack, with generated parameters such as the ID
     * @throws QuackDataAccessException If whatever error occurs
     * @see QuackDao#update(int, java.lang.String)
     * @see QuackDao#remove(java.lang.String, int)
     */
    public Quack postNew(Quack q) throws QuackDataAccessException {
        try {
            Set<Hashtag> persisted = new HashSet<>();
            q.getHashTags().forEach(t -> {
                String ident = t.getIdentifier();
                t = em.find(Hashtag.class, ident);
                if (t == null) {
                    em.persist(t = new Hashtag(ident));
                }
                persisted.add(t);
            });
            q.setHashTags(new ArrayList<>(persisted));
            q = em.merge(q);
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaUpdate<User> upd = b.createCriteriaUpdate(User.class);
            Root<User> rt = upd.from(User.class);
            upd.set("lastestActivity", q.getPostDate());
            upd.where(b.equal(rt, q.getAuthor()));
            em.createQuery(upd).executeUpdate();
            protocol("User #" + q.getAuthor().getId() + " posted quack #" + q.getId());
            return q;
        } catch (Throwable e) {
            throw getException(e);
        }
    }

    /**
     * Posts a new comment. Author ID and quack id must always be set
     *
     * @param comm Comment to be posted
     * @return Comment with generated parameters such as the comment ID
     * @throws QuackDataAccessException If whatever error occurs
     */
    public Comment postComment(Comment comm) throws QuackDataAccessException {
        try {
            comm = em.merge(comm);
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaUpdate<User> upd = b.createCriteriaUpdate(User.class);
            Root<User> rt = upd.from(User.class);
            upd.set("lastestActivity", comm.getPostDate());
            upd.where(b.equal(rt, comm.getAuthor()));
            em.createQuery(upd).executeUpdate();
            protocol("User #" + comm.getAuthor().getId() + " posted Comment #" + comm.getId() + " on quack #" + comm.getQuack().getId());
            return comm;
        } catch (Throwable t) {
            throw new QuackDataAccessException(Comment.class, t);
        }
    }
    
    /**
     * Deletes an existing comment. Author ID and quack id must always be set
     *
     * @param cid ID of the comment to be deleted
     * @param authorEmail eMail of the author of the comment
     * @return Comment with generated parameters such as the comment ID
     * @throws QuackDataAccessException If whatever error occurs
     */
    public boolean deleteComment(String authorEmail, int cid) throws QuackDataAccessException {
        try {
            Comment c = em.find(Comment.class, cid);
            if (c != null && c.getAuthor().getEmail().equals(authorEmail)) {
                em.remove(c);
                return true;
            }
            return false;
        } catch (Throwable t) {
            throw new QuackDataAccessException(Comment.class, t);
        }
    }

    /**
     * Updates the content of the given quack id
     *
     * @param email eMail of the author of the quack
     * @param pid ID of the quack to be updated
     * @param content New content of the quack
     * @return true if the quack has been successfully updated, otherwise false
     * @throws QuackDataAccessException If whatever error occurs
     */
    public boolean update(String email, int pid, String content) throws QuackDataAccessException {
        try {
            Quack q = em.find(Quack.class, pid);
            if (q != null && q.getAuthor().getEmail().equalsIgnoreCase(email)) {
                q.setContent(content);
                protocol("Quack #" + pid + " updated");
                return true;
            }
            return false;
        } catch (Exception e) {
            throw getException(e);
        }
    }

    /**
     * Removes the specified quack from the system if it belongs to the
     * specified user
     *
     * @param email Principal of the current user. Must be the author of the
     * quack to be deleted
     * @param qid Quack to be deleted from the system
     * @return true if the quack has been successfully deleted, otherwise false
     * @throws QuackDataAccessException If whatever error occurs
     */
    public boolean remove(String email, int qid) throws QuackDataAccessException {
        try {
            Quack q = em.find(Quack.class, qid);
            if (q != null && (email == null || q.getAuthor().getEmail().equalsIgnoreCase(email))) {
                em.remove(q);
                protocol("Quack #" + qid + " removed");
                return true;
            }
            return false;
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Loads the comments list of the given quack.
     *
     * @param quackId ID of the quack that contains the comments to be loaded
     * @param offset Start offset of the list to be returned
     * @param limit Maximal count of the quacks to be returned
     * @return List of the found comments meeting the specified criteria
     * @throws QuackDataAccessException If whatever error occurs
     */
    public List<Comment> getComments(int quackId, int offset, int limit) throws QuackDataAccessException {
        try {
            Quack q = em.find(Quack.class, quackId);
            List<Comment> comms = new ArrayList<>();
            if (q != null && q.getComments() != null && !q.getComments().isEmpty()) {
                comms.addAll(q.getComments());
                comms.sort((Comment a, Comment b) -> b.getPostDate().compareTo(a.getPostDate()));
                comms = comms.subList(offset, offset + limit);
            }
            return comms;
        } catch (Throwable t) {
            throw new QuackDataAccessException(Comment.class, t);
        }
    }

    /**
     * Loads the list of the most recently posted quacks that the given user has
     * the right to read.
     *
     * @param email Principal of the current user
     * @param offset Start offset of the list to be returned
     * @param count Maximal count of the quacks to be returned
     * @param since Index of quack, where the returned quacks are newer than it
     * @return List of quacks that met the criteria
     * @throws QuackDataAccessException If whatever error occurs
     * @see QuackDao#findAll(int, int)
     */
    public List<Quack> getRecent(String email, int offset, int count, int since) throws QuackDataAccessException {
        try {
            // SELECT q FROM Quack q WHERE NOT EXISTS (SELECT u FROM User u WHERE u.email = :email AND u MEMBER OF q.author.blockedUsers) AND (q.scope = 2 OR (q.scope <= 1 AND q.author.email = :email) OR (q.scope = 1 AND EXISTS (SELECT u FROM User u WHERE u.email = :email AND q.author MEMBER OF u.followedUsers))) ORDER BY q.postDate DESC
            // SELECT q FROM Quack q WHERE EXISTS (SELECT u FROM User u WHERE u.email = :email AND u NOT MEMBER OF q.author.blockedUsers) AND (q.scope = 2 OR (q.scope <= 1 AND q.author.email = :email) OR (q.scope = 1 AND EXISTS (SELECT u FROM User u WHERE u.email = :email AND q.author MEMBER OF u.followedUsers))) ORDER BY q.postDate DESC
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Quack> q = b.createQuery(Quack.class);
            Root<Quack> rt = q.from(Quack.class);
            return em.createQuery(q.where(generateRestrictions(b, q, rt, email), b.greaterThan(rt.<Integer>get("id"), since)).orderBy(b.desc(rt.get("postDate"))))
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Constructs predicate to a given query in order to restrict quack
     * visibility depending on the scope.
     *
     * @param b Default CriteriaBuilder object
     * @param q Default query object
     * @param from Root object (quack)
     * @param email eMail of the user, whose visibility to quacks is about to be
     * restricted
     * @return Constructed predicate object
     */
    private Predicate generateRestrictions(CriteriaBuilder b, CriteriaQuery<Quack> q, Root<Quack> from, String email) {
        // Check if the given user is blocked by the author of the quack (not-exists subquery)
        Subquery<User> sub1 = q.subquery(User.class);
        Root<User> sub1Root = sub1.from(User.class);
        sub1.select(sub1Root);
        sub1.where(b.equal(sub1Root.get("email"), email), b.not(b.isMember(sub1Root, from.<User>get("author").<Collection<User>>get("blockedUsers"))));
        // ------- Check scopes -------
        // Public scope
        Predicate pred2a = b.equal(from.<Short>get("scope"), GlobalConstants.SCOPE_PUBLIC);
        // Private and followers scope
        Predicate pred2b = b.and(b.lessThanOrEqualTo(from.<Short>get("scope"), (short) GlobalConstants.SCOPE_FOLLOWERS), b.equal(from.get("author").get("email"), email));
        Subquery<User> sub2 = q.subquery(User.class);
        Root<User> sub2from = sub2.from(User.class);
        sub2.select(sub2from);
        sub2.where(b.equal(sub2from.get("email"), email), b.isMember(from.<User>get("author"), sub2from.<Collection<User>>get("followedUsers")));
        Predicate pred2c = b.and(b.equal(from.get("scope"), 1), b.exists(sub2));
        // Make scope restrictions together
        Predicate pred2 = b.or(pred2a, pred2b, pred2c);
        // Block & scope restrictions
        return b.and(b.exists(sub1), pred2);
    }
    
    /**
     * Protocol activities and store them into database
     * @param infos Activities to protocol
     * @return 
     */
    private int protocol(String... infos) {
        if (infos != null && infos.length > 0) {
            StringBuilder baseSql = new StringBuilder("INSERT INTO log (info) VALUES");
            for (int i = 0; i < infos.length; i++) {
                if (i > 0) {
                    baseSql.append(",");
                }
                baseSql.append(" (:info")
                        .append(i)
                        .append(")");
            }
            Query q = em.createNativeQuery(baseSql.toString());
            for (int i = 0; i < infos.length; i++) {
                q.setParameter("info" + i, infos[i]);
            }
            try {
                return q.executeUpdate();
            } catch (Throwable e) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Private method that generates a QuackDataAccessException based on the
     * given parameter.
     *
     * @param t Original exception
     * @return Generated exception
     */
    private QuackDataAccessException getException(Throwable t) {
        return new QuackDataAccessException(Quack.class, t);
    }
}
