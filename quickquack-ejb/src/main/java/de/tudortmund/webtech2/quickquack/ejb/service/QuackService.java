package de.tudortmund.webtech2.quickquack.ejb.service;

import de.tudortmund.webtech2.quickquack.ejb.dao.QuackDao;
import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.entity.Comment;
import de.tudortmund.webtech2.quickquack.ejb.entity.Hashtag;
import de.tudortmund.webtech2.quickquack.ejb.entity.Quack;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import de.tudortmund.webtech2.quickquack.ejb.other.GlobalConstants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

@RequestScoped
public class QuackService {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private QuackDao dao;
    @Inject
    private UserDao uDao;

    /**
     * Finds the quacks that the user with the given eMail can read.
     * @param email
     *          eMail of the user that is about to read the posts
     * @param from
     *          Start offset of the list
     * @param limit
     *          Maximal count of the quacks to be returned (at least 0, at most 10)
     * @param since
     *          Index of first quack, after which quack list should be returned
     * @return
     * @throws QuackServiceException 
     */
    public List<Quack> get(String email, int from, int limit, int since) throws QuackServiceException {
        if (from < 0) {
            from = 0;
        }
        if (limit < 0) {
            limit = -limit;
        }
        if (limit > 10) {
            limit = 10;
        }
        try {
            return dao.getRecent(email, from, limit, since);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Quacks können nicht geladen werden");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten");
        }
    }

    /**
     * Reads a single Quack
     * @param email
     *          eMail of the user reading the quack
     * @param id
     *          ID of the quack to be returned
     * @return Quack with the given ID if found, otherwise null
     * @throws QuackServiceException 
     */
    public Quack getSingle(String email, int id) throws QuackServiceException {
        if (id < 0) {
            throw new QuackServiceException("ID ungültig");
        }
        try {
            return dao.findById(email, id);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Quack nicht gefunden");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten");
        }
    }

    /**
     * Stores the given quack into the database
     * @param q
     *          Quack to be merged
     * @return Quack with refreshed fields, such as hashtags and ID.
     * @throws QuackServiceException 
     */
    public Quack post(Quack q) throws QuackServiceException {
        if (q == null || q.getContent() == null) {
            throw new QuackServiceException("Quackinhalt ist leer!");
        }
        try {
            String content = q.getContent().trim();
            Set<Hashtag> tags = new HashSet<>();
            boolean inTag = false;
            String temp = "";
            for (int i = 0, l = content.length(); i < l; i++) { // Parse hashtags
                char curChar = content.charAt(i);
                if (inTag) {
                    if (curChar == ' ' || curChar == '\n') {
                        if (!temp.isEmpty()) {
                            tags.add(new Hashtag(temp));
                            temp = "";
                        }
                        inTag = false;
                    } else {
                        temp += curChar;
                    }
                } else if (curChar == '#') {
                    inTag = true;
                }
            }
            if (inTag && !temp.isEmpty()) {
                tags.add(new Hashtag(temp));
            }
            q.setContent(content);
            q.setHashTags(new ArrayList<>(tags));
            return dao.postNew(q);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Quack kann nicht hinzugefügt werden");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten");
        }
    }
    
    /**
     * Posts a new comment
     * @param c
     *          Comment to be posted. Author and Quack must be set.
     * @return Comment with filled attributes
     * @throws QuackServiceException 
     */
    public Comment post(Comment c) throws QuackServiceException {
        try {
            Quack q = c.getQuack();
            User commentAuthor = c.getAuthor(), quackAuthor = q.getAuthor();
            if (!SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS) &&
                    !commentAuthor.equals(quackAuthor) && (quackAuthor.getBlockedUsers().contains(commentAuthor) || q.getScope() == GlobalConstants.SCOPE_PRIVATE || (q.getScope() == GlobalConstants.SCOPE_FOLLOWERS && !c.getAuthor().getFollowedUsers().contains(quackAuthor)))) {
                logger.info(commentAuthor.getEmail() + " tried to illegally comment an quack... cheater!");
                throw new QuackServiceException("Sie haben keine Berechtigung, auf dieses Quack zu kommentieren.");
            }
            c.setPostDate(DateTime.now());
            return dao.postComment(c);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException("Fehler beim speichern des Kommentars");
        } catch (QuackServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new QuackServiceException(t);
        }
    }

    /**
     * Updates the given quack content. (Based on it's ID)
     * @param email
     *          eMail of the author of the quack.
     * @param qid
     *          ID of the quack to be updated.
     * @param content
     *          New content of the quack.
     * @return true if quack successfully updated, false otherwise
     * @throws QuackServiceException 
     */
    public boolean update(String email, int qid, String content) throws QuackServiceException {
        if (qid < 0 || content == null) {
            throw new QuackServiceException("Quackinhalt ist leer!");
        }
        try {
            return dao.update(email, qid, content);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Quack kann nicht aktualisiert werden");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten");
        }
    }

    /**
     * Removes a quack from the system, if it belongs to the user with the given eMail.
     * @param email
     *          eMail of the user (author of the quack to be deleted)
     * @param id
     *          ID of the quack to be deleted.
     * @return true if quack successfully deleted, otherwise false
     * @throws QuackServiceException 
     */
    public boolean remove(String email, int id) throws QuackServiceException {
        try {
            return dao.remove(email, id);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Quack kann nicht gelöscht werden");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten");
        }
    }

    /**
     * Sets/unsets like to the quack with the given ID
     * @param email
     *              eMail of the user setting the like.
     * @param postId
     *              ID of the quack to be liked.
     * @param like
     *              True to like, false to unlike
     * @throws QuackServiceException 
     */
    public void setLike(String email, int postId, boolean like) throws QuackServiceException {
        if (email == null|| postId < 0) {
            throw new QuackServiceException("Parameter ungültig");
        }
        try {
            Quack q = dao.findById(email, postId);
            User u = uDao.findByEmail(email);
            if (q.getAuthor().equals(u) ||
                    (!q.getAuthor().getBlockedUsers().contains(u) &&
                    (q.getScope() == GlobalConstants.SCOPE_PUBLIC ||
                    (q.getScope() == GlobalConstants.SCOPE_FOLLOWERS && u.getFollowedUsers().contains(q.getAuthor()))))) {
                
            }
            if (like) {
                if (!q.getLikers().contains(u)) {
                    q.getLikers().add(u);
                }
            } else {
                q.getLikers().remove(u);
            }
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Like kann nicht gesetzt werden", true);
        }
    }
    
    /**
     * Changes the scope of the quack with the given ID if it belongs to the user with the given eMail.
     * @param email
     *          eMail of the author of the quack
     * @param quackId
     *          ID of the quack, whose scope is about to be changed
     * @param scope
     *          New value of the scope (between 0-2)
     * @return true if scope successfully updated, otherwise false
     * @throws QuackServiceException 
     */
    public boolean changeScope(String email, int quackId, short scope) throws QuackServiceException {
        if (scope < 0 || scope > 2)
            return false;
        try {
            dao.changeScope(email, quackId, scope);
            return true;
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException("Scope kann nicht geändert werden");
        }
    }
}
