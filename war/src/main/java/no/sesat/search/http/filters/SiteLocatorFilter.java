/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * SiteLocatorFilter.java
 *
 * Created on 9 February 2006, 11:30
 */

package no.sesat.search.http.filters;



import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.UrlResourceLoader;
import no.sesat.search.site.Site;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.view.FindResource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/** Loads the appropriate Site object in as a request attribute.
 * Will redirect to correct (search-front-config) url for resources (css,images, javascript). <br/>
 * Also responsible for logging each request and response like an apache access logfile.
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class SiteLocatorFilter implements Filter {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SiteLocatorFilter.class);
    private static final Logger ACCESS_LOG = Logger.getLogger("no.sesat.Access");

    private static final String ERR_NOT_FOUND = "Failed to find resource ";
    private static final String ERR_UNCAUGHT_RUNTIME_EXCEPTION
            = "Following runtime exception was let loose in tomcat against ";

    private static final String INFO_USING_DEFAULT_LOCALE = " is falling back to the default locale ";
    private static final String DEBUG_REQUESTED_VHOST = "Virtual host is ";
    private static final String DEBUG_REDIRECTING_TO = " redirect to ";
    private static final String WARN_FAULTY_BROWSER = "Site in datamodel does not match requested site. User agent is ";

    private static final String PUBLISH_DIR = "/img/";
    
    private static final String UNKNOWN = "unknown";

    
    private static final String CONFIGURATION_RESOURCE= "/conf/" + Site.CONFIGURATION_FILE;

    /** Changes to this list must also change the ProxyPass|ProxyPassReverse configuration in httpd.conf **/
    private static final Collection<String> EXTERNAL_DIRS =
            Collections.unmodifiableCollection(Arrays.asList(new String[]{
                PUBLISH_DIR, "/css/", "/images/", "/javascript/"
    }));

    /** The context that we'll need to use every invocation of doFilter(..)  **/
    public static final Site.Context SITE_CONTEXT = new Site.Context(){
        public String getParentSiteName(final SiteContext siteContext) {
            // we have to do this manually instead of using SiteConfiguration,
            //  because SiteConfiguration relies on the parent site that we haven't get initialised.
            // That is, the PARENT_SITE_KEY property MUST be explicit in the site's configuration.properties.
            final Properties props = new Properties();
            final PropertiesLoader loader
                    = UrlResourceLoader.newPropertiesLoader(siteContext, Site.CONFIGURATION_FILE, props);
            loader.abut();
            return props.getProperty(Site.PARENT_SITE_KEY);
        }
    };

    // Attributes ----------------------------------------------------

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;
    private static final String LOCALE_DETAILS = "Locale details: Language: {0}, Country: {1} and Variant: {2}";

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Default constructor. **/
    public SiteLocatorFilter() {
    }

    // Public --------------------------------------------------------

    /** Will redirect to correct (search-config) url for resources (css,images, javascript).
     *
     * @param request The servlet request we are processing
     * @param r The servlet response
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    //@Override jdk 1.6
    public void doFilter(
            final ServletRequest request,
            final ServletResponse r,
            final FilterChain chain)
                throws IOException, ServletException {

        LOG.trace("doFilter(..)");

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final ServletResponse response = r instanceof HttpServletResponse
            ? new AccessLogResponse((HttpServletResponse)r)
            : r;

        try{
            if(request instanceof HttpServletRequest) {
                final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                final HttpServletResponse  httpServletResponse = (HttpServletResponse) response;
                if (httpServletRequest.getRequestURI().endsWith(CONFIGURATION_RESOURCE)){
                    /* We are looping, looking for a site search which does not exsist */
                    LOG.debug("We are looping, looking for a site search which does not exist");
                    httpServletResponse.reset();
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            doBeforeProcessing(request, response);
            logAccessRequest(request);

            if (request instanceof HttpServletRequest) {

                final HttpServletRequest req = (HttpServletRequest)request;
                final HttpServletResponse res = (HttpServletResponse) response;
                final Site site = (Site) req.getAttribute(Site.NAME_KEY);
                final String uri = req.getRequestURI();
                final String resource = uri;
                final String rscDir = resource != null && resource.indexOf('/',1) >= 0
                        ? resource.substring(0, resource.indexOf('/',1)+1)
                        : null;
                
                if(isAccessAllowed(req)){

                    if (rscDir != null && EXTERNAL_DIRS.contains(rscDir)) {

                        // This URL does not belong to search-portal
                        final String url = FindResource.find(site, resource);
                        
                        if (url != null) {
                            // Cache the client-resource redirects on a short (session-equivilant) period
                            res.setHeader("Cache-Control", "Public"); 
                            res.setDateHeader("Expires", System.currentTimeMillis() + 1000*60*10); // ten minutes
                            // send the redirect to where the resource really resides
                            res.sendRedirect(url);
                            LOG.trace(resource + DEBUG_REDIRECTING_TO + url);
                            
                            
                        }else if (resource.startsWith(PUBLISH_DIR)){
                            // XXX Why do we avoid sending 404 for publish resources?
                            
                            res.sendError(HttpServletResponse.SC_NOT_FOUND);

                            if(resource.endsWith(".css")){
                                LOG.info(ERR_NOT_FOUND + resource);
                            }else{
                                LOG.error(ERR_NOT_FOUND + resource);
                            }                            
                        }

                    } else  {
                        doChainFilter(chain, request, response);
                    }
                    
                }else{
                    // Forbidden client
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                
            }  else  {
                doChainFilter(chain, request, response);
            }

            doAfterProcessing(request, response);

        }  catch (Exception e) {
            // Don't let anything through without logging it.
            //  Otherwise it ends in a different logfile.
            LOG.error(ERR_UNCAUGHT_RUNTIME_EXCEPTION);
            for (Throwable t = e; t != null; t = t.getCause()) {
                LOG.error(t.getMessage(), t);
            }
            throw new ServletException(e);
            
        }finally{
            logAccessResponse(request, response, stopWatch);
        }

    }

    /**
     * Return the filter configuration object for this filter.
     * @return 
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
    //@Override jdk 1.6
    public void destroy() {
    }


    /**
     * Init method for this filter
     *
     */
    //@Override jdk 1.6
    public void init(final FilterConfig filterConfig) {

        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            LOG.debug("Initializing filter");
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {

        return filterConfig == null
                ? "ResourceRedirectFilter()"
                : "ResourceRedirectFilter(" + filterConfig + ")";

    }

    /** The method to obtain the correct Site from the request.
     * It only returns a site with a locale supported by that site.
     ** @param servletRequest 
     * @return 
     */
    public static Site getSite(final ServletRequest servletRequest) {
        // find the current site. Since we are behind a ajp13 connection request.getServerName() won't work!
        // httpd.conf needs:
        //      1) "JkEnvVar SERVER_NAME" inside the virtual host directive.
        //      2) "UseCanonicalName Off" to assign ServerName from client's request.
        final String vhost = getServerName(servletRequest);


        LOG.trace(DEBUG_REQUESTED_VHOST + vhost);

        // Construct the site object off the browser's locale, even if it won't finally be used.
        final Locale locale = servletRequest.getLocale();
        final Site result = Site.valueOf(SITE_CONTEXT, vhost, locale);
        final SiteConfiguration.Context siteConfCxt = new SiteConfiguration.Context(){
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {

                return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public Site getSite() {
                return result;
            }
        };
        final SiteConfiguration siteConf = SiteConfiguration.valueOf(siteConfCxt);
        servletRequest.setAttribute(SiteConfiguration.NAME_KEY, siteConf);

        if(LOG.isTraceEnabled()){ // MessageFormat.format(..) is expensive
            LOG.trace(MessageFormat.format(
                    LOCALE_DETAILS, locale.getLanguage(), locale.getCountry(), locale.getVariant()));
        }
        
        // Check if the browser's locale is supported by this skin. Use it if so.
        if( siteConf.isSiteLocaleSupported(locale) ){
            return result;
        }
        
        // Use the skin's default locale. For some reason that fails use JVM's default.
        final String[] prefLocale = null != siteConf.getProperty(SiteConfiguration.SITE_LOCALE_DEFAULT)
                ? siteConf.getProperty(SiteConfiguration.SITE_LOCALE_DEFAULT).split("_")
                : new String[]{Locale.getDefault().toString()};

        switch(prefLocale.length){

            case 3:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE + prefLocale[0] + '_' + prefLocale[1] + '_' + prefLocale[2]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0], prefLocale[1], prefLocale[2]));

            case 2:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE + prefLocale[0] + '_' + prefLocale[1]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0], prefLocale[1]));

            case 1:
            default:
                LOG.trace(result+INFO_USING_DEFAULT_LOCALE + prefLocale[0]);
                return Site.valueOf(SITE_CONTEXT, vhost, new Locale(prefLocale[0]));

        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private static void doChainFilter(
            final FilterChain chain,
            final ServletRequest request,
            final ServletResponse response) throws IOException, ServletException{
        
        Lock lock = request instanceof HttpServletRequest 
                ? (Lock) ((HttpServletRequest)request).getSession().getAttribute("userLock")
                : new ReentrantLock();
        
        if(null == lock){
            lock = new ReentrantLock();
            ((HttpServletRequest)request).getSession().setAttribute("userLock", lock);
        }
        
        // datamodel is NOT request-safe. all the user's requests must execute in sequence!
        //  ten seconds is enough to wait.
        boolean done = false;
        try{
            if( lock.tryLock(10, TimeUnit.SECONDS) ){  
                try{
                    // request will be processed by search-portal
                    LOG.info("Incoming! Duck!");
                    chain.doFilter(request, response);
                    done = true;
                }finally{
                    lock.unlock();
                }
            }
        }catch(InterruptedException ie){
            LOG.warn("The duck got trucked", ie);
        }
        
        if(!done && response instanceof HttpServletResponse){
            // failed to get lock in time
            LOG.warn(" -- response 409");
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_CONFLICT);
        }
    }

    static String getRequestId(final ServletRequest servletRequest){

        return null != servletRequest.getAttribute("UNIQUE_ID")
                ? (String)servletRequest.getAttribute("UNIQUE_ID")
                : UUID.randomUUID().toString();
    }

    private void doBeforeProcessing(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        LOG.trace("doBeforeProcessing()");

        final Site site = getSite(request);

        final DataModel dataModel = getDataModel(request);

        if (null != dataModel && !dataModel.getSite().getSite().equals(site)) {
            LOG.warn(WARN_FAULTY_BROWSER + dataModel.getBrowser().getUserAgent().getString());
            // DataModelFilter will correct it
        }

        request.setAttribute(Site.NAME_KEY, site);
        request.setAttribute("startTime", FindResource.START_TIME);
        MDC.put(Site.NAME_KEY, site.getName());
        MDC.put("UNIQUE_ID", getRequestId(request));
        
        /* Setting default encoding */
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }

    private void doAfterProcessing(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        LOG.trace("doAfterProcessing()");
        //
        // Write code here to process the request and/or response after
        // the rest of the filter chain is invoked.
        //

    }

    private static DataModel getDataModel(final ServletRequest request){

        DataModel datamodel = null;
        if(request instanceof HttpServletRequest){
            final HttpServletRequest httpRequest = (HttpServletRequest)request;
            final HttpSession session = httpRequest.getSession(false);
            if(null != session){
                datamodel = (DataModel) session.getAttribute(DataModel.KEY);
            }
        }
        return datamodel;
    }

    private static void logAccessRequest(final ServletRequest request){
       
        final StringBuilder url = new StringBuilder();
        final String referer;
        final String method;
        final String ip = request.getRemoteAddr();
        final String userAgent;
        final String sesamId;
        final String sesamUser;
        
        if(request instanceof HttpServletRequest){  
            
            final HttpServletRequest req = (HttpServletRequest)request;
            url.append(req.getRequestURI() + (null != req.getQueryString() ? '?' + req.getQueryString() : ""));
            referer = req.getHeader("Referer");
            method = req.getMethod();
            userAgent = req.getHeader("User-Agent");
            sesamId = getCookieValue(req, "SesamID");
            sesamUser = getCookieValue(req, "SesamUser");

            
        }else{
            
            for(@SuppressWarnings("unchecked")
                    Enumeration<String> en = request.getParameterNames(); en.hasMoreElements(); ){
                
                final String param = en.nextElement();
                url.append(param + '=' + request.getParameter(param));
                if(en.hasMoreElements()){
                    url.append('&');
                }
            }
            referer = method = userAgent = sesamId = sesamUser = UNKNOWN;
        }
        
        ACCESS_LOG.info("<request>"
                + "<url method=\"" + method + "\">" + StringEscapeUtils.escapeXml(url.toString()) + "</url>"
                + (null != referer ? "<referer>" + StringEscapeUtils.escapeXml(referer) + "</referer>" : "")
                + "<browser ipaddress=\"" + ip + "\">" + StringEscapeUtils.escapeXml(userAgent) + "</browser>"
                + "<user id=\"" + sesamId + "\">" + sesamUser + "</user>"
                + "</request>");
    }
    
    private static void logAccessResponse(
            final ServletRequest request, 
            final ServletResponse response,
            final StopWatch stopWatch){
       
        final String code;
        
        if(request instanceof HttpServletRequest){  
            
            final HttpServletRequest req = (HttpServletRequest)request;
            
        }else{
            
        }
        
        if(response instanceof AccessLogResponse){  
            
            final AccessLogResponse res = (AccessLogResponse)response;
            code = String.valueOf(res.getStatus());
            
        }else{
            
            code = UNKNOWN;
        }
        
        stopWatch.stop();
        
        ACCESS_LOG.info("<response code=\"" + code + "\" time=\"" + stopWatch + "\"/>");
    }
    
    // probably apache commons could simplify this // duplicated in SearchServlet
    private static String getCookieValue(final HttpServletRequest request, final String cookieName){
    
        String value = "";
        // Look in attributes (it could have already been updated this request)
        if( null != request ){

            // Look through cookies
            if( null != request.getCookies() ){
                for( Cookie c : request.getCookies()){
                    if( c.getName().equals( cookieName ) ){
                        value = c.getValue();
                        break;
                    }
                }
            }
        }

        return value;
    }
    
    private static String getServerName(final ServletRequest servletRequest){
        
        // find the current site. Since we are behind a ajp13 connection request.getServerName() won't work!
        // httpd.conf needs:
        //      1) "JkEnvVar SERVER_NAME" inside the virtual host directive.
        //      2) "UseCanonicalName Off" to assign ServerName from client's request.
        return null != servletRequest.getAttribute("SERVER_NAME")
            ? (String) servletRequest.getAttribute("SERVER_NAME")
            // falls back to this when not behind Apache. (Development machine).
            : servletRequest.getServerName() + ":" + servletRequest.getServerPort();
    }
    
    private static boolean isAccessAllowed(final HttpServletRequest request){
        
        final SiteConfiguration siteConf = (SiteConfiguration) request.getAttribute(SiteConfiguration.NAME_KEY);
        final String allowedList = siteConf.getProperty(SiteConfiguration.ALLOW_LIST);
        final String disallowedList = siteConf.getProperty(SiteConfiguration.DISALLOW_LIST);
        final String ipaddress = request.getRemoteAddr();
        
        boolean allowed = false;
        boolean disallowed = false;
        if(null != allowedList && 0 < allowedList.length()){
            for(String allow : allowedList.split(",")){
                allowed |= ipaddress.startsWith(allow);
            }
        }else{
            allowed = true;
        }
        if(null != disallowedList && 0 < disallowedList.length()){
            for(String disallow : disallowedList.split(",")){
                disallowed |= ipaddress.startsWith(disallow);
            }
        }
        return allowed && !disallowed;  
    }
    
    private static class AccessLogResponse extends HttpServletResponseWrapper{
        
        private int status = HttpServletResponse.SC_OK;
        
        public AccessLogResponse(final HttpServletResponse response){
            super(response);
        }
        
        @Override
        public void setStatus(final int status){
            super.setStatus(status);
            this.status = status;
        }
        @Override
        public void setStatus(final int status, final String msg){
            super.setStatus(status, msg);
            this.status = status;
        }
        @Override
        public void sendError(final int sc) throws IOException{
            super.sendError(sc);
            status = sc;
        }
        @Override
        public void sendError(final int sc, final String msg) throws IOException{
            super.sendError(sc, msg);
            status = sc;
        }
        @Override
        public void sendRedirect(final String arg0) throws IOException {
            super.sendRedirect(arg0);
            this.status = HttpServletResponse.SC_FOUND;
        }
        
        public int getStatus(){
            return status;
        }
              
    }
}
