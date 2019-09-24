package de.tudortmund.webtech2.quickquack.ejb.security;

import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

/**
 * Class required by Shiro (no further development required)
 * @author salim
 */
public class CustomSaltedAuthenticationInfo implements SaltedAuthenticationInfo {
    private static final long serialVersionUID = -5467967895187234984L;
    private final String username;
    private final String password;
    private final String salt;

    public CustomSaltedAuthenticationInfo(String username, String password, String salt) {
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    @Override
    public ByteSource getCredentialsSalt() {
        return new SimpleByteSource(Base64.decode(salt)); 
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(username, username);
    }

    @Override
    public Object getCredentials() {
        return password;
    }
}
