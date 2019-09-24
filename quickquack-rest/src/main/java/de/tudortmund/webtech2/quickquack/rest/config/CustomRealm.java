package de.tudortmund.webtech2.quickquack.rest.config;

import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.security.CustomSaltedAuthenticationInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.joda.time.DateTime;

/**
 * Shiro configuration realm
 * @author salim
 */
public class CustomRealm extends JdbcRealm {
    private Logger logger = Logger.getLogger(getClass());
    
    public CustomRealm() {
        super();
        setSaltStyle(SaltStyle.COLUMN);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;
        final String username = userPassToken.getUsername();

        if (username == null) {
            logger.log(Level.WARN, "No username provided. Aborting");
            return null;
        }

        User user = null;
        try {
            try (Connection conn = dataSource.getConnection(); PreparedStatement pst = conn.prepareStatement("SELECT * FROM user WHERE email = ?")) {
                pst.setString(1, username);
                user = mapResultSetToUser(pst.executeQuery());
            }
        } catch (SQLException e) {
            logger.log(Level.INFO, "Login failure for " + username);
        }

        if (user == null) {
            logger.log(Level.INFO, "No account found for user [" + username + "]");
            return null;
        } else if (user.getAccountActive() == 0)
            throw new LockedAccountException();
        
        return new CustomSaltedAuthenticationInfo(username, user.getPassword(), user.getPasswordSalt());
    }

    private static User mapResultSetToUser(ResultSet set) throws SQLException {
        User u = null;
        if (set != null) {
            if (set.next()) {
                u = new User();
                u.setId(set.getInt("id"));
                u.setEmail(set.getString("email"));
                u.setAlias(set.getString("alias"));
                u.setPassword(set.getString("password"));
                u.setPasswordSalt(set.getString("password_salt"));
                u.setLastestActivity(new DateTime(set.getDate("lastest_activity")));
                u.setAccountActive(set.getShort("account_active"));
            }
            set.close();
        }
        return u;
    }
}
