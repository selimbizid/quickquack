package de.tudortmund.webtech2.quickquack.rest.interceptor;

import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.CommentDto;
import de.tudortmund.webtech2.quickquack.ejb.dto.QuackDto;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.apache.shiro.SecurityUtils;
import org.jboss.logging.Logger;

/**
 * Intercept client requests containing Quacks or Comments as body parameter and sets the correct author ID.
 * @author salim
 */
@Interceptor
public class ClientDtoInterceptor {
    private final Logger logger = Logger.getLogger(getClass());
    @Inject
    private UserDao dao;
    
    @AroundInvoke
    public Object setUserId(InvocationContext ctx) throws Exception {
        String principal = SecurityUtils.getSubject().getPrincipal().toString();
        logger.info("Intercepting client request of " + principal + "...");
        Object[] params = ctx.getParameters();
        int currentUserId = dao.getIdByEmail(principal);
        for (Object p : params) {
            if (p instanceof QuackDto) {
                QuackDto q = (QuackDto) p;
                q.setAuthorId(currentUserId);
                q.setPostDate(null);
                logger.info("Quack author ID updated to " + currentUserId);
            } else if (p instanceof CommentDto) {
                CommentDto d = (CommentDto) p;
                d.setAuthorId(currentUserId);
                d.setPostDate(null);
                logger.info("Comment author ID updated to " + currentUserId);
            }
        }
        try {
            return ctx.proceed();
        } catch (Exception e) {
            return null;
        }
    }
}
