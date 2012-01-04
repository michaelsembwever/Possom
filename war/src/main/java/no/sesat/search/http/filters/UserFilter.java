/* Copyright (2007-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be usefu,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * UserFilter.java
 *
 * Created on 9 March 2007, 15:25
 */
package no.sesat.search.http.filters;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import no.sesat.search.user.BasicUser;
import no.sesat.search.user.service.InvalidTokenException;
import no.sesat.search.user.service.UserCookieUtil;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.user.service.BasicUserService;
import org.apache.log4j.Logger;

/**
 * Responsible for Persistent User Login. Or "Remember Me" functionality. Based off
 * http://fishbowl.pastiche.org/2004/01/19/persistent_login_cookie_best_practice
 *
 * The user's manual logging in with username and password
 *  must be performed in a separate application that fronts to UserService.
 *
 * @xxx it may be more appropriate that this class belongs in sesat-user project?
 *
 * @version <tt>$Id$</tt>
 */
public final class UserFilter implements Filter {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(UserFilter.class);

    private static final String DISABLE_LOGOUT = "sesat.user.logout.disabled";
    private static final String ACTION_PARAMETER = "action";
    private static final String LOGOUT_PARAMETER_VALUE = "logout";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Default constructor. */
    public UserFilter() {
        super();
    }

    // Public --------------------------------------------------------

    /**
     * The filter action method.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response for the request
     * @param chain The filter chain we are processing
     * @exception IOException Thrown if an input/output error occurs
     * @exception ServletException Thrown if a servlet error occurs
     */
    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            initialiseUserCookie((HttpServletRequest) request, (HttpServletResponse) response);
            performAutomaticLogin((HttpServletRequest) request, (HttpServletResponse) response);

        }
        if(!response.isCommitted()){
            chain.doFilter(request, response);
        }
    }

    /**
     * Destroy method for this filter.
     */
    @Override
    public void destroy() {
    }


    /**
     * Init method for this filter.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(final FilterConfig filterConfig) {
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    /**
     * Method that populate the user datamodel if no user is set and there exists a login cookie.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response for the request
     */
    private static void performAutomaticLogin(final HttpServletRequest request, final HttpServletResponse response) {

        final HttpSession session = request.getSession();
        final DataModel datamodel = (DataModel) session.getAttribute(DataModel.KEY);

        final BasicUserService basicUserService = getBasicUserService(datamodel);

        if (null != basicUserService) {
            final String loginKey = UserCookieUtil.getUserLoginCookie(request);
            final boolean isLegalLoginKey = basicUserService.isLegalLoginKey(loginKey);

            final BasicUser user = datamodel.getUser().getUser();
            final Date updateTimestamp = UserCookieUtil.getUserUpdateCookie(request);

            final boolean actionLogout = LOGOUT_PARAMETER_VALUE.equals(request.getParameter(ACTION_PARAMETER));

            if (user == null && isLegalLoginKey) {

                // Login if no user and a legal login key.
                loginUsingCookie(loginKey, datamodel, basicUserService, response);

            } else if (user != null && (actionLogout || !isLegalLoginKey)) {

                // Check if the user should be logged out, no login key anymore.
                logout(datamodel, basicUserService, response);

                // Remove the logout from the url to prevent problems with sesamBackUrl.
                if (actionLogout) {

                    final String queryString = request.getQueryString()
                            .replaceFirst("&?" + ACTION_PARAMETER + '=' + LOGOUT_PARAMETER_VALUE, "");

                    redirect(request.getRequestURL() + "?" + queryString, response);
                }

            } else if (null != user && isLegalLoginKey && user.isDirty(updateTimestamp)) {

                    // Check if the user object is dirty, refresh if needed.
                    LOG.info("Logged in user dirty, refreshes: " + user.getFullName());
                    datamodel.getUser().setUser(basicUserService.refreshUser(user));
            }
        }else{
            LOG.debug("Couldn't find the basic user service.");
        }
    }

    /**
     * Initializing the personalization session from the login key.
     *
     * @param loginKey the login key used for login
     * @param datamodel the data model
     * @param userService the user service
     * @param response the request response
     */
    private static void loginUsingCookie(
            final String loginKey,
            final DataModel datamodel,
            final BasicUserService basicUserService,
            final HttpServletResponse response) {

        LOG.info("Login user with login key: " + loginKey);

        try {
            final BasicUser user = basicUserService.authenticateByLoginKey(loginKey);

            if (null != user) {

                datamodel.getUser().setUser(user);

                // Updates the login cookie.
                UserCookieUtil.setUserLoginCookie(response, user.getNextLoginKey());
            }
        } catch (InvalidTokenException e) {
            // TODO: Give message to user? eg "You were logged out for security reasons"
            LOG.warn("Invalid token in login key: " + loginKey);
            datamodel.getUser().setUser(null);
            UserCookieUtil.setUserLoginCookieDefault(response);
        } catch (Throwable e) {
            // TODO: Give message to user?  eg "You were logged out for security reasons"
            LOG.warn("Unknown throwable: " + e.getMessage());
            datamodel.getUser().setUser(null);
            UserCookieUtil.setUserLoginCookieDefault(response);
        }
    }

    /**
     * Method used to reset a session totally.
     * It removes the user object from the datamodel, calls basicUserService.invalidateLogin(loginKey)
     *  and resets the loginKey cookie to its default value.
     *
     * The invalidateLogin(..) call and cookie reset can be disabled by setting in the skin's configuration.properties
     * sesat.user.logout.disabled=true
     *
     * but the user will always be removed from the datamodel,
     *  and expected to be re-inserted on the next loginUsingCookie(..) call.
     *
     * @param datamodel the datamodel
     * @param userService the user service
     * @param response the request response
     */
    private static void logout(
            final DataModel datamodel,
            final BasicUserService userService,
            final HttpServletResponse response) {

        final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();

        if(!Boolean.parseBoolean(siteConf.getProperty(DISABLE_LOGOUT))){

            final BasicUser user = datamodel.getUser().getUser();
            LOG.info("Logout: " + user.getFullName());

            if (userService.isLegalLoginKey(user.getNextLoginKey())) {
                userService.invalidateLogin(user.getNextLoginKey());
            }

            UserCookieUtil.setUserLoginCookieDefault(response);
        }
        datamodel.getUser().setUser(null);
    }

    /**
     * Place a cookie into the response so any subsequent requests can trust that cookies are enabled.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response for the request
     */
    private static void initialiseUserCookie(final HttpServletRequest request, final HttpServletResponse response) {

        if (null == UserCookieUtil.getUserLoginCookie(request)) {

            // The user is not logged in.
            // Place the cookie, so we can test that cookies are enabled.
            UserCookieUtil.setUserLoginCookieDefault(response);
        }
    }

    private static void redirect(final String url, final HttpServletResponse response) {

        try {
            response.sendRedirect(url);

        } catch (final IOException e) {
            LOG.error(e);
        }
    }

    /**
     * Return the basic user service used for personalization.
     *
     * @param datamodel the data model
     * @return the user service
     */
    private static BasicUserService getBasicUserService(final DataModel datamodel) {

        final boolean enabled = Boolean.parseBoolean(
                datamodel.getSite().getSiteConfiguration().getProperty("sesat.userservice.enabled"));

        if (enabled && !Boolean.getBoolean("jnp.disableDiscovery")) {

            // lookup the ejb3-client service
            final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();
            final String url = siteConf.getProperty("schibstedsok_remote_service_url");
            final String jndi = siteConf.getProperty("user_service_jndi_name");

            LOG.debug("Url: " + url);
            LOG.debug("JndiName: " + jndi);

            if( null != url && null != jndi ){

                final Properties properties = new Properties();
                properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
                properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
                properties.put("java.naming.provider.url", url);

                try {
                    return (BasicUserService) new InitialContext(properties).lookup(jndi);

                } catch (final NamingException ne) {
                    // acceptable for possom not to have to have a user service backend
                    LOG.debug(ne.getMessage(), ne);
                }
            }
        }
        return null;
    }

}
