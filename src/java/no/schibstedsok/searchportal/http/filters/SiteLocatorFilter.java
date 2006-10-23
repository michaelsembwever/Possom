// Copyright (2006) Schibsted Søk AS
/*
 * SiteLocatorFilter.java
 *
 * Created on 9 February 2006, 11:30
 */

package no.schibstedsok.searchportal.http.filters;



import java.io.IOException;
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
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import no.schibstedsok.searchportal.site.Site;
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
            = "Following runtime exception was let loose in tomcat against ";

    private static final String INFO_USING_DEFAULT_LOCALE = " is falling back to the default locale ";
    private static final String DEBUG_REQUESTED_VHOST = "Virtual host is ";
    private static final String DEBUG_REDIRECTING_TO = " redirect to ";

    private static final String HTTP = "http://";
    private static final String PUBLISH_DIR = "/img/";

    /** Changes to this list must also change the ProxyPass|ProxyPassReverse configuration in httpd.conf **/
    private static final Collection<String> EXTERNAL_DIRS =
            Collections.unmodifiableCollection(Arrays.asList(new String[]{
                PUBLISH_DIR, "/css/", "/images/", "/javascript/"
    }));

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;

    /** TODO comment me. **/
    public static final Site.Context SITE_CONTEXT = new Site.Context(){
        public String getParentSiteName(final SiteContext siteContext) {
            // we have to do this manually instead of using SiteConfiguration,
            //  because SiteConfiguration relies on the parent site that we haven't get initialised.
            final Properties props = new Properties();
            final PropertiesLoader loader
                    = UrlResourceLoader.newPropertiesLoader(siteContext, Site.CONFIGURATION_FILE, props);
            loader.abut();
            return props.getProperty(Site.PARENT_SITE_KEY);
        }
    };

    private static final long START_TIME = System.currentTimeMillis();

    /** TODO comment me. **/
    public SiteLocatorFilter() {
    }

    private void doBeforeProcessing(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        LOG.trace("doBeforeProcessing()");

        final Site site = getSite(request);
        request.setAttribute(Site.NAME_KEY, site);
        request.setAttribute("startTime", START_TIME);
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
        final HttpServletRequest req = (HttpServletRequest) request;

        try  {

            doBeforeProcessing(request, response);

            if (request instanceof HttpServletRequest) {
                
                final HttpServletResponse res = (HttpServletResponse) response;
                final String uri = req.getRequestURI();
                final String resource = uri;
                final String rscDir = resource != null && resource.indexOf('/',1) >= 0
                        ? resource.substring(0, resource.indexOf('/',1)+1)
                        : null;

                if (rscDir != null && EXTERNAL_DIRS.contains(rscDir)) {

                    // This URL does not belong to search-front-html
                    final Site site = (Site) req.getAttribute(Site.NAME_KEY);
                    String url = "";

                    if (resource.startsWith(PUBLISH_DIR)) { // publishing system
                        // the publishing system is responsible for this.
                        final Properties props = SiteConfiguration.valueOf(site).getProperties();
                        url = props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL)
                            .replaceFirst("localhost",props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_HOST))
                            + '/' + resource;

                    }  else  {
                        // strip the version number out of the resource
                        final String noVersionRsc = resource.replaceFirst("/(\\d)+/","/");

                        // Find resource in current site or any of its
                        // ancestors
                        url = recursivelyFindResource(noVersionRsc, site);

                        if (url == null) {
                            res.sendError(HttpServletResponse.SC_NOT_FOUND);
                            url = null;
                            if(resource.endsWith(".css")){
                                LOG.info(ERR_NOT_FOUND + resource);
                            }else{
                                LOG.error(ERR_NOT_FOUND + resource);
                            }
                        }
                    }

                    if (url != null) {
                        res.sendRedirect(url);
                        LOG.trace(resource + DEBUG_REDIRECTING_TO + url);
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

        }  catch (Exception e) {
            // Don't let anything through without logging it.
            //  Otherwise it ends in a different logfile.
            LOG.error(ERR_UNCAUGHT_RUNTIME_EXCEPTION + req.getQueryString());
            for (Throwable t = e; t != null; t = e.getCause()) {
                LOG.error("", t);
            }
            throw new ServletException(e);
        }

    }


    private String recursivelyFindResource(final String resource, final Site site) {
        
        final String datedResource = resource
                .replaceAll("/", "/" + START_TIME + "/")
                .replaceFirst("/" + START_TIME + "/", "");
        
        final String url = HTTP + site.getName() + site.getConfigContext() + '/' + datedResource;

        if (UrlResourceLoader.urlExists(url)) {
            // return a relative url to ensure it can survice through an out-of-cluster server.
            return '/' + site.getConfigContext() + '/' + datedResource;
        } else if (site.getParent() != null) {
            return recursivelyFindResource(resource, site.getParent());
        } else {
            return null;
        }
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
     * It only returns a site with a locale supported by that site.
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
        
        
        LOG.trace(DEBUG_REQUESTED_VHOST + vhost);

        // Construct the site object off the browser's locale, even if it won't finally be used.
        final Locale locale = servletRequest.getLocale();
        final Site result = Site.valueOf(SITE_CONTEXT, vhost, locale);
        final SiteConfiguration.Context siteConfCxt = new SiteConfiguration.Context(){// <editor-fold defaultstate="collapsed" desc=" genericCxt ">
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public Site getSite() {
                return result;
            }
        };//</editor-fold>
        final SiteConfiguration siteConf = SiteConfiguration.valueOf(siteConfCxt);

        // Check if the browser's locale is supported by this skin. Use it if so.
        if( siteConf.isSiteLocaleSupported(locale) ){
            return result;
        }

        // Use the skin's default locale.
        final String[] prefLocale = siteConf.getProperty(SiteConfiguration.SITE_LOCALE_DEFAULT).split("_");

        switch(prefLocale.length){
            
            case 3:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE + prefLocale[0]
                        + '_' + prefLocale[1] + '_' + prefLocale[2]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0], prefLocale[1], prefLocale[2]));
                
            case 2:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE
                        + prefLocale[0] + '_' + prefLocale[1]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0], prefLocale[1]));
                
            case 1:
            default:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE
                        + prefLocale[0]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0]));
                
        }


    }
    
    

}
