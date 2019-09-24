package de.tudortmund.webtech2.quickquack.ejb.service;

import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import de.tudortmund.webtech2.quickquack.ejb.other.GlobalTools;
import de.tudortmund.webtech2.quickquack.ejb.security.CustomSaltedAuthenticationInfo;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.subject.Subject;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

@RequestScoped
public class UserService {

    final private Logger logger = Logger.getLogger(getClass());

    @Inject
    private UserDao userDao;

    public User tryLogin(Subject currentUser, String username, String password) throws QuackServiceException {
        User u;
        try {
            u = userDao.findByEmail(username);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Benutzer kann nicht gefunden werden.");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten.");
        }
        if (u.getAccountActive() == 0) {
            throw new QuackServiceException("Konto gesperrt.");
        }
        HashedCredentialsMatcher h = getCredentialsMatcher();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        CustomSaltedAuthenticationInfo info = new CustomSaltedAuthenticationInfo(u.getEmail(), u.getPassword(), u.getPasswordSalt());
        if (h.doCredentialsMatch(token, info)) {
            return u;
        } else {
            throw new QuackServiceException("Passwort falsch.");
        }
    }

    public boolean changeAlias(String email, String alias) throws QuackServiceException {
        if (!GlobalTools.nonEmptyStrings(email, alias)) {
            return false;
        }
        try {
            return userDao.updateAlias(email, alias);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e);
        }
    }

    public User registerUser(String email, String plainPassword) throws QuackServiceException {
        logger.info("Registration request from " + email);
        if (!GlobalTools.nonEmptyStrings(email, plainPassword)) {
            throw new QuackServiceException("eMail und/oder Password ungultig.");
        }
        User u = new User();
        u.setEmail(email);
        u.setLastestActivity(DateTime.now());
        u.setAccountActive((short) 1);
        try {
            generatePassword(u, plainPassword);
            return userDao.mergeUser(u);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, e.hasCustomMessage() ? e.getMessage() : "Konto kann nicht angelegt werden.");
        } catch (Throwable t) {
            throw new QuackServiceException(t, "Fehler aufgetreten.");
        }
    }

    public boolean changePassword(String email, String old, String newp) throws QuackServiceException {
        User u = null;
        try {
            u = userDao.findByEmail(email);
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e, "Benutzer nicht gefunden");
        }
        if (u != null) {
            HashedCredentialsMatcher h = getCredentialsMatcher();
            UsernamePasswordToken token = new UsernamePasswordToken(u.getEmail(), old);
            CustomSaltedAuthenticationInfo info = new CustomSaltedAuthenticationInfo(u.getEmail(), u.getPassword(), u.getPasswordSalt());
            if (h.doCredentialsMatch(token, info)) {
                generatePassword(u, newp);
                try {
                    userDao.updatePassword(u.getId(), u.getPassword(), u.getPasswordSalt());
                    logger.info("Password successfully changed.");
                    return true;
                } catch (QuackDataAccessException e) {
                    throw new QuackServiceException(e, "Passwort konnte nicht geändert werden.");
                } catch (Throwable t) {
                    throw new QuackServiceException(t, "Fehler aufgetreten.");
                }
            } else {
                throw new QuackServiceException("Das alte Passwort ist falsch.");
            }
        }
        return false;
    }

    public User changePassword(User u, String old, String newp) throws QuackServiceException {
        if (u == null || !GlobalTools.nonEmptyStrings(old, newp)) {
            throw new QuackServiceException("Ungültige Parameter");
        }
        logger.info("Password change request from user: " + u.getEmail());
        HashedCredentialsMatcher h = getCredentialsMatcher();
        UsernamePasswordToken token = new UsernamePasswordToken(u.getEmail(), old);
        CustomSaltedAuthenticationInfo info = new CustomSaltedAuthenticationInfo(u.getEmail(), u.getPassword(), u.getPasswordSalt());
        if (h.doCredentialsMatch(token, info)) {
            generatePassword(u, newp);
            logger.info("Password successfully changed.");
            try {
                return userDao.mergeUser(u);
            } catch (QuackDataAccessException e) {
                throw new QuackServiceException(e, "Password konnte nicht geändert werden.");
            } catch (Throwable t) {
                throw new QuackServiceException(t, "Fehler aufgetreten");
            }
        } else {
            throw new QuackServiceException("Der alte Passwort ist falsch");
        }
    }

    public boolean follow(String followerEmail, int followedId) throws QuackServiceException {
        try {
            User follower = userDao.findByEmail(followerEmail), followed = userDao.findById(followedId);
            if (follower.getFollowedUsers().contains(followed)) {
                follower.getFollowedUsers().remove(followed);
                return false;
            } else {
                follower.getFollowedUsers().add(followed);
                return true;
            }
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e.getCause(), "Benutzer konnte nicht gefolgt werden");
        }
    }
    
    public void follow(String followerEmail, int followedId, boolean follow) throws QuackServiceException {
        try {
            User follower = userDao.findByEmail(followerEmail), followed = userDao.findById(followedId);
            if (follow) {
                if (!follower.getFollowedUsers().contains(followed)) {
                    follower.getFollowedUsers().add(followed);
                }
            } else {
                follower.getFollowedUsers().remove(followed);
            }
        } catch (QuackDataAccessException e) {
            throw new QuackServiceException(e.getCause(), "Benutzer konnte nicht gefolgt werden");
        }
    }

    private HashedCredentialsMatcher getCredentialsMatcher() {
        HashedCredentialsMatcher hcm = new HashedCredentialsMatcher();
        hcm.setHashIterations(1024);
        hcm.setHashAlgorithmName("SHA-512");
        hcm.setStoredCredentialsHexEncoded(false);
        return hcm;
    }

    private void generatePassword(User user, String plainTextPassword) {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha512Hash(plainTextPassword, salt, 1024).toBase64();
        user.setPassword(hashedPasswordBase64);
        user.setPasswordSalt(salt.toString());
    }
}
