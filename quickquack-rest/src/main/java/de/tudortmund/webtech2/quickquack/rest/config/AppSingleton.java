package de.tudortmund.webtech2.quickquack.rest.config;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;
import org.apache.shiro.mgt.SecurityManager;

/**
 * Singleton provided by the container.
 * All initializations should be done here.
 * @author salim
 */
@Startup
@Singleton
public class AppSingleton {
    @PostConstruct
    public void init() {
        // Initialize Shiro SecurityUtils
        Factory<SecurityManager> factory = new IniSecurityManagerFactory();
        SecurityUtils.setSecurityManager(factory.getInstance());
    }
}
