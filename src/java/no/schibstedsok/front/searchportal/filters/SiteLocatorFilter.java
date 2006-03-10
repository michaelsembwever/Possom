// Copyright (2006) Schibsted Søk AS
/*
 * SiteLocatorFilter.java
 *
 * Created on 9 February 2006, 11:30
 */

package no.schibstedsok.front.searchportal.filters;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/** Loads the appropriate Site object in as a request attribute.
 * Will redirect to correct (search-front-config) url for resources (css,images, javascript).
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class SiteLocatorFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(SiteLocatorFilter.class);

    private static final String ERR_NOT_FOUND = "Failed to find resource ";
    private static final String ERR_UNCAUGHT_RUNTIME_EXCEPTION
            = "Following runtime exception was let loose in tomcat\n";
    private static final String DEBUG_REQUESTED_VHOST = "Virtual host is ";
    private static final String DEBUG_REDIRECTING_TO = " redirect to ";

    private static final String HTTP = "http://";

    private static final String PUBLISH_DIR = "img/";

    private static final Collection/*<String>*/ EXTERNAL_DIRS =
            Collections.unmodifiableCollection(Arrays.asList(new String[]{
                PUBLISH_DIR, "css/", "images/", "javascript/"
    }));

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;

    public SiteLocatorFilter() {
    }

    private void doBeforeProcessing(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        LOG.trace("doBeforeProcessing()");

        final Site site = getSite(request);
        request.setAttribute(Site.NAME_KEY, site);
        MDC.put(Site.NAME_KEY, site.getName());
    }

    private void doAfterProcessing(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        LOG.trace("doAfterProcessing()");
        //
        // Write code here to process the request and/or response after
        // the rest of the filter chain is invoked.
        //

    }

    /** Will redirect to correct (search-front-config) url for resources (css,images, javascript).
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
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

        LOG.trace("doFilter(..)");

        try  {

            doBeforeProcessing(request, response);

            if (request instanceof HttpServletRequest) {
                final HttpServletRequest req = (HttpServletRequest) request;
                final HttpServletResponse res = (HttpServletResponse) response;
                final String uri = req.getRequestURI();
                final String resource = uri.substring(uri.indexOf('/', 1) + 1);
                final String rscDir = resource != null && resource.indexOf('/') >= 0
                        ? resource.substring(0, resource.indexOf('/')+1)
                        : null;

                if (rscDir != null && EXTERNAL_DIRS.contains(rscDir) ) {

                    // This URL does not belong to search-front-html
                    final Site site = (Site) req.getAttribute(Site.NAME_KEY);
                    String url = "";

                    if (resource.startsWith(PUBLISH_DIR)) { // publishing system
                        // the publishing system is responsible for this.
                        final Properties props = XMLSearchTabsCreator.valueOf(site).getProperties();
                        url = props.getProperty(SearchConstants.PUBLISH_SYSTEM_URL)
                        .replaceFirst("localhost",props.getProperty(SearchConstants.PUBLISH_SYSTEM_HOST))
                            + '/' + resource;

                    }  else  {
                        // search-front-config is responsible for this.
                        // But first we must find which layer will serve it.
                        url = HTTP + site.getName() + site.getConfigContext() + resource;
                        if (!urlExists(url)) {
                            url = HTTP + Site.DEFAULT.getName() + Site.DEFAULT.getConfigContext() + resource;
                            if (!urlExists(url)) {
                                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                                url = null;
                                LOG.error(ERR_NOT_FOUND + resource);
                            }
                        }
                    }

                    if (url != null) {
                        res.sendRedirect(url);
                        LOG.debug(resource + DEBUG_REDIRECTING_TO + url);
                    }

                } else  {
                    // request will be processed by search-front-html
                    chain.doFilter(request, response);
                }
            }  else  {
                // request will be processed by search-front-html
                chain.doFilter(request, response);
            }


            doAfterProcessing(request, response);

        }  catch (RuntimeException e) {
            // Don't let anything through without logging it.
            //  Otherwise it ends in a different logfile.
            LOG.error(ERR_UNCAUGHT_RUNTIME_EXCEPTION);
            for (Throwable t = e; t != null; t = e.getCause()) {
                LOG.error("", t);
            }
            throw e;
        }

    }

    private boolean urlExists(final String url) {

        boolean success = false;
        HttpURLConnection con = null;
        try {

            final URL u = new URL(UrlResourceLoader.getURL(url));

            con = (HttpURLConnection) u.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("HEAD");
            con.addRequestProperty("host", UrlResourceLoader.getHostHeader(url));
            success = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

        } catch (NullPointerException e) {
            LOG.debug(url, e);

        } catch (IOException e) {
            LOG.warn(url, e);
        }  finally  {
            if (con != null) {
                con.disconnect();
            }
        }

        return success;
    }


    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (filterConfig);
    }


    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(final FilterConfig filterConfig) {

        this.filterConfig = filterConfig;
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

        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            LOG.debug("Initializing filter");
        }
    }

    /**
     * Return a String representation of this object.
     */
    public String toString() {

        return filterConfig == null
                ? "ResourceRedirectFilter()"
                : "ResourceRedirectFilter(" + filterConfig + ")";

    }

    /** The method to obtain the correct Site from the request.
     *
     **/
    public static Site getSite(final ServletRequest servletRequest) {
        // find the current site. Since we are behind a ajp13 connection request.getServerName() won't work!
        // httpd.conf needs:
        //      1) "JkEnvVar SERVER_NAME" inside the virtual host directive.
        //      2) "UseCanonicalName Off" to assign ServerName from client's request.
        final String vhost = null != servletRequest.getAttribute("SERVER_NAME")
            ? (String) servletRequest.getAttribute("SERVER_NAME")
            // falls back to this when not behind Apache. (Development machine).
            : servletRequest.getServerName() + ":" + servletRequest.getServerPort();

        // Just because many norwegians have their computers installed in english mode
        //  we can't presume they want their webpages in english.
        //  Therefore we must always initially replace english locales with norwegian.
        final Locale requestLocale = servletRequest.getLocale();
        final Locale locale = "en".equals(requestLocale.getLanguage())
                ? Locale.getDefault()
                : requestLocale;

        LOG.debug(DEBUG_REQUESTED_VHOST + vhost);

        return Site.valueOf(vhost, locale);
    }

}
