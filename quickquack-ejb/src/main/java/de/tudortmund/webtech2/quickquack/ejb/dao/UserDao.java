package de.tudortmund.webtech2.quickquack.ejb.dao;

import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import java.util.Collection;
import java.util.List;
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
import javax.validation.ValidationException;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Data access object that handles the users.
 * Provided by the EJB container.
 */
@Dependent
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * @return List of available users on the system. Should be used only with users with permission BAN_USERS
     * @throws QuackDataAccessException if whatever error occurs
     */
    public List<User> findAll() throws QuackDataAccessException {
        try {
            return em.createNamedQuery("User.findAll", User.class).getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * @param email
     *              eMail of the user to be looked for
     * @return ID of the user that has the given email. -1 if whatever error occurs.
     */
    public int getIdByEmail(String email) {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Integer> q = b.createQuery(Integer.class);
            Root<User> root = q.from(User.class);
            q.select(root.get("id"));
            q.where(b.equal(root.get("email"), email));
            return em.createQuery(q).getSingleResult();
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * 
     * @param userId
     *              ID of the user to be looked for
     * @return User object if exists
     * @throws QuackDataAccessException  if whatever error occurs or user doesn't exist.
     */
    public User findById(int userId) throws QuackDataAccessException {
        try {
            return em.find(User.class, userId);
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * @param alias
     *          Alias of the user to be looked for.
     * @return User object if exists
     * @throws QuackDataAccessException if whatever error occurs or user doesn't exist.
     */
    public User findByAlias(String alias) throws QuackDataAccessException {
        TypedQuery<User> q = em.createNamedQuery("User.findByAlias", User.class);
        q.setParameter("alias", alias);
        try {
            return q.getSingleResult();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * @param email
     *          eMail of the user to be looked for.
     * @return User object if exists.
     * @throws QuackDataAccessException if whatever error occurs or user doesn't exist.
     */
    public User findByEmail(String email) throws QuackDataAccessException {
        TypedQuery<User> q = em.createNamedQuery("User.findByEmail", User.class);
        q.setParameter("email", email);
        try {
            return q.getSingleResult();
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * Returns the list of the users following the user with the given email.
     * @param email
     *          eMail of the user
     * @return List of following users, if given user exist. Otherwise null is returned
     * @throws QuackDataAccessException 
     */
    public Collection<User> getFollowing(String email) throws QuackDataAccessException {
        try {
            User u = findByEmail(email);
            if (u != null) {
                return u.getFollowingUsers();
            }
            return null;
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * Returns the list of the users followed by the user with the given email.
     * @param email
     *          eMail of the user
     * @return List of the followed users, if given user exist. Otherwise null is returned
     * @throws QuackDataAccessException 
     */
    public Collection<User> getFollowed(String email) throws QuackDataAccessException {
        try {
            User u = findByEmail(email);
            if (u != null) {
                return u.getFollowedUsers();
            }
            return null;
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Finds the list of users, whose email or alias contains the given parameter.
     * @param regex
     *          Regex to be looked for.
     * @return List of the found users.
     * @throws QuackDataAccessException if whatever error occurs.
     */
    public List<User> findByAliasOrEmail(String regex, int offset, int count) throws QuackDataAccessException {
        try {
            regex = '%' + regex + '%';
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<User> qr = b.createQuery(User.class);
            Root<User> from = qr.from(User.class);
            qr.select(from);
            Predicate emailPr = b.like(from.get("email"), regex);
            Predicate aliasPr = b.like(from.get("alias"), regex);
            qr.where(b.or(emailPr, aliasPr));
            return em.createQuery(qr)
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .getResultList();
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Merges the given user into the system (e.g for registration).
     * @param u
     *          User to be merged.
     * @return User object with assigned ID.
     * @throws QuackDataAccessException If a user that has the same email address exists.
     */
    public User mergeUser(User u) throws QuackDataAccessException {
        try {
            u = em.merge(u);
            protocol("User #" + u.getId() + " (" + u.getEmail() + ") merged");
            return u;
        } catch (Throwable t) {
            Throwable loop = t;
            
            do {
                if (loop instanceof ConstraintViolationException) {
                    throw new QuackDataAccessException(User.class, t, "eMail Adresse existiert bereits!");
                }
                if (loop instanceof ValidationException) {
                    throw new QuackDataAccessException(User.class, t, "eMail Adresse ungültig.");
                }
            } while ((loop = loop.getCause()) != null);
            
            throw getException(t);
        }
    }
    
    /**
     * Updates the alias of the given user
     * @param email
     *          Principal of the current user, whose alias is about to be changed.
     * @param alias
     * @return true if alias has been changed, otherwise false
     * @throws QuackDataAccessException If whatever error occurs
     */
    public boolean updateAlias(String email, String alias) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            if (em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :alias AND u.email != :email", Long.class)
                    .setParameter("alias", alias)
                    .setParameter("email", email).getSingleResult() == 0) {
                CriteriaUpdate<User> up = b.createCriteriaUpdate(User.class);
                Root<User> rt = up.from(User.class);
                up.set("alias", alias);
                up.where(b.equal(rt.get("email"), email));
                if (em.createQuery(up).executeUpdate() > 0) {
                    protocol("User " + email + " updated alias to " + alias);
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * Changes the password of the given user.
     * @param uid
     *          ID of the user, whose password is about to be changed.
     * @param pass
     *          New password (hashed).
     * @param salt
     *          Salt of the hash.
     * @return true if password successfully changed, otherwise false.
     * @throws QuackDataAccessException if whatever error occurs.
     */
    public boolean updatePassword(int uid, String pass, String salt) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaUpdate<User> upd = b.createCriteriaUpdate(User.class);
            Root<User> rt = upd.from(User.class);
            upd.set("password", pass);
            upd.set("passwordSalt", salt);
            upd.where(b.equal(rt.get("id"), uid));
            return em.createQuery(upd).executeUpdate() > 0;
        } catch (Throwable t) {
            throw getException(t);
        }
    }

    /**
     * Changes the account activity of the given user (ban/deban).
     * @param accountId
     *              ID of the account, whose activity is about to be changed.
     * @param active
     *              true if account should be activated, or false to deactivate it
     * @return true in activity updated, otherwise false
     * @throws QuackDataAccessException 
     */
    public boolean setAccountActivity(int accountId, boolean active) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaUpdate<User> upd = b.createCriteriaUpdate(User.class);
            Root<User> rt = upd.from(User.class);
            upd.set("accountActive", active ? 1 : 0);
            upd.where(b.equal(rt.get("id"), accountId));
            if (em.createQuery(upd).executeUpdate() > 0) {
                protocol("Account #" + accountId + (active ? " de" : " ") + "blocked");
                return true;
            }
            return false;
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * Checks if the account with the given email is active
     * @param email
     *          eMail of the account to be checked
     * @return true if account is active, otherwise false
     * @throws QuackDataAccessException 
     */
    public boolean isActive(String email) throws QuackDataAccessException {
        try {
            CriteriaBuilder b = em.getCriteriaBuilder();
            CriteriaQuery<Short> qr = b.createQuery(Short.class);
            Root<User> from = qr.from(User.class);
            qr.select(from.get("accountActive"));
            qr.where(b.equal(from.get("email"), email));
            return em.createQuery(qr).getSingleResult() == 1;
        } catch (NoResultException e) {
            return false;
        } catch (Throwable t) {
            throw getException(t);
        }
    }
    
    /**
     * User with the second parameter can't see the posts of the user with the given email address
     * @param blockerEmail
     *              eMail of the blocking user
     * @param blockedId
     *              ID of the blocked user
     * @param block
     *              true to activate block, otherwise false to deactivate it
     * @throws QuackDataAccessException 
     */
    public void block(String blockerEmail, int blockedId, boolean block) throws QuackDataAccessException {
        try {
            User blocker = findByEmail(blockerEmail), blocked = em.find(User.class, blockedId);
            if (block && !blocker.getBlockedUsers().contains(blocked)) {
                blocker.getBlockedUsers().add(blocked);
                em.merge(blocker);
            } else {
                blocker.getBlockedUsers().remove(blocked);
                protocol("User #" + blockedId + " were " + (block? "" : "de") + "blocked by " + blockerEmail);
            }
        } catch (Throwable t) {
            throw new QuackDataAccessException(User.class, t);
        }
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
            return q.executeUpdate();
        }
        return 0;
    }
    
    private QuackDataAccessException getException(Throwable t) {
        return new QuackDataAccessException(User.class, t);
    }
}
