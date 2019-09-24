package de.tudortmund.webtech2.quickquack.rest.filter;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.transaction.UserTransaction;

/**
 * Filter that lets user transaction open, avoiding LazyInitializationExceptions: Open Session in View pattern
 * URL paths should be set in web.xml file.
 * @author salim
 */
public class UserTransactionFilter implements Filter {
    @Resource
    private UserTransaction utx;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            utx.begin();
            chain.doFilter(request, response);
            utx.commit();
        } catch (Exception e) { }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException { }
    
    @Override
    public void destroy() { }
}
