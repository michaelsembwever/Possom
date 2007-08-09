/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * UserFilter.java
 *
 * Created on 9 March 2007, 15:25
 */

package no.schibstedsok.searchportal.http.filters;

import java.io.IOException;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.user.UserCookie;
import no.schibstedsok.searchportal.user.service.UserService;
import org.apache.log4j.Logger;

/** Responsible for Persistent User Login.
 * Or "Remember Me" functionality.
 * Based off http://fishbowl.pastiche.org/2004/01/19/persistent_login_cookie_best_practice
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class UserFilter implements Filter {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(UserFilter.class);

    private static final String USER_COOKIE_KEY ="SesamUser";
    private static final String USER_COOKIE_PATH = "/";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /**
     * 
     */
    public UserFilter() {
    }


    // Public --------------------------------------------------------

    /**
     *
     * @param request The servlet request we are processing
     * @param response 
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain)
                throws IOException, ServletException {

        if(request instanceof HttpServletRequest){
            final HttpServletRequest httpRequest = (HttpServletRequest)request;

            initialiseUserCookie(httpRequest, (HttpServletResponse)response);

            performAutomaticLogin(httpRequest, (HttpServletResponse)response);

            chain.doFilter(request, response);

        }else{
            chain.doFilter(request, response);
        }
    }

    /**
     * Destroy method for this filter
     *
     */
    public void destroy() {
    }


    /**
     * Init method for this filter
     *
     */
    public void init(final FilterConfig filterConfig) {
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    /**  Look for a User cookie.
     * Can return null. **/
    private static Cookie getUserCookie(final HttpServletRequest request){

        // Look in attributes (it could have already been updated this request)
        if( null != request ){

            // Look through cookies
            if( null != request.getCookies() ){
                for( Cookie c : request.getCookies()){
                    if( c.getName().equals( USER_COOKIE_KEY ) ){
                        return c;
                    }
                }
            }
        }

        return null;
    }

    private static void performAutomaticLogin(
            final HttpServletRequest request,
            final HttpServletResponse response){

        final HttpSession session = request.getSession();
        final DataModel datamodel = (DataModel) session.getAttribute(DataModel.KEY);

        if(null == datamodel.getUser().getUser()){

            final Cookie cookie = getUserCookie(request);
            if(null != cookie && !"0".equals(cookie.getValue())){

                // lookup the ejb3-client service
                final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();
                final String url = siteConf.getProperty("schibstedsok_remote_service_url");
                final String jndi = siteConf.getProperty("user_service_jndi_name");

				LOG.info("Url: " + url);
				LOG.info("JNDI_NAME: " + jndi);

                final Properties properties = new Properties();
				properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
				properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
				properties.put("java.naming.provider.url", url);

                try{

                    final InitialContext ctx = new InitialContext(properties);
                    final UserService service = (UserService) ctx.lookup(jndi);

                    // perform the login
                    final String automatedLoginKey = cookie.getValue();

                    final UserCookie userCookie = service.getUserByAutomaticId(automatedLoginKey);
                    datamodel.getUser().setUser(userCookie.getUser());

                    // update the UserCookie ready for next automaticLogin
                    response.addCookie(createUserCookie(userCookie.getAutomaticId()));

                }catch(NamingException ne){
                    LOG.error(ne.getMessage(), ne);
                }
            }
        }
    }

    /** Place a cookie into the response so on any subsequent requests can cookies are enabled.
     **/
    private static void initialiseUserCookie(
            final HttpServletRequest request,
            final HttpServletResponse response){

        final Cookie cookie = getUserCookie(request);

        if( cookie == null ){
            // The user is not logged in
            // Place the cookie, so we can test cookies are enabled
            response.addCookie(createUserCookie("0"));
        }
    }

    private static Cookie createUserCookie(final String content){

        final Cookie cookie = new Cookie(USER_COOKIE_KEY, content);
        cookie.setPath(USER_COOKIE_PATH);
        cookie.setMaxAge(Integer.MAX_VALUE);

        return cookie;
    }

}
