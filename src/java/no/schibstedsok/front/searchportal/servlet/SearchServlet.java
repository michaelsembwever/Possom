// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.servlet;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchModeFactory;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import no.schibstedsok.front.searchportal.util.QueryStringHelper;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.query.run.QueryFactory;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.view.i18n.TextMessages;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import no.schibstedsok.front.searchportal.view.config.SearchTabFactory;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.log4j.Logger;

/** The Central Controller to incoming queries.
 * Controls the SearchMode -> RunningQuery creation and handling.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SearchServlet extends HttpServlet {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);
    private static final String ERR_MISSING_TAB = "No existing implementation for tab ";
    private static final String ERR_MISSING_MODE = "No existing implementation for mode ";
    private static final String WARN_TABS_CLEANED = " status on cleaning site for ";
    private static final String WARN_CONFIG_CLEANED = " status on cleaning configuration for ";
    private static final String WARN_MODES_CLEANED = " status on cleaning modes for ";

    //private SearchTabs tabs;

    /** {@inheritDoc}
     */
    public void destroy() {
        //tabs.stopAll();
    }

    /** {@inheritDoc}
     */
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {


        if ( isEmptyQuery(request, response) ) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: doGet()");
            LOG.debug("Character encoding ="  + request.getCharacterEncoding());
        }

        StopWatch stopWatch = null;
        if (LOG.isInfoEnabled()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        final Site site = (Site) request.getAttribute(Site.NAME_KEY);
        
        performReloads(site, request.getParameter("reload"));

        updateContentType(site, response);

        String searchTabKey = request.getParameter("c");

        if (searchTabKey == null) {
            searchTabKey = "d";
        }

        // BaseContext providing SiteContext and ResourceContext.
        //  We need it casted as a SiteContext for the ResourceContext code to be happy.
        final SiteContext genericCxt = new SiteContext(){// <editor-fold defaultstate="collapsed" desc=" genericCxt ">
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite() {
                return site;
            }
        };//</editor-fold>

        final SearchTab searchTab = SearchTabFactory.getTabFactory(
                ContextWrapper.wrap(SearchTabFactory.Context.class, genericCxt))
                .getTabByKey(searchTabKey);
        
        if( searchTab == null ){
            LOG.error(ERR_MISSING_TAB + searchTabKey);
            throw new UnsupportedOperationException(ERR_MISSING_TAB + searchTabKey);
        }
        
        final SearchMode mode = SearchModeFactory.getModeFactory(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());
        
        if( mode == null ){
            LOG.error(ERR_MISSING_MODE + searchTab.getMode());
            throw new UnsupportedOperationException(ERR_MISSING_MODE + searchTab.getMode());
        }

        final RunningQuery.Context rqCxt = ContextWrapper.wrap(// <editor-fold defaultstate="collapsed" desc=" rqCxt ">
                RunningQuery.Context.class,
                new BaseContext() {
                    public SearchMode getSearchMode() {
                        return mode;
                    }
                    public SearchTab getSearchTab() {
                        return searchTab;
                    }
                },
                genericCxt
        );//</editor-fold>

        final RunningQuery query = QueryFactory.getInstance()
            .createQuery(rqCxt, request, response);

        request.setAttribute("locale", query.getLocale());
        request.setAttribute("query", query);
        request.setAttribute("site", site);
        request.setAttribute("text",
                TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, genericCxt)));

        if (request.getParameter("offset") != null
                && !"".equals(request.getParameter("offset"))) {

            query.setOffset(Integer.parseInt(request.getParameter("offset")));
        }

        if (request.getParameter("q") != null) {
            request.setAttribute("q",
                QueryStringHelper.safeGetParameter(request, "q"));
        }

        request.setAttribute("tab", searchTab);
        request.setAttribute("c", searchTabKey);

        try {
                query.run();
        } catch (InterruptedException e) {
            LOG.error("Task timed out");
        }

        if (LOG.isInfoEnabled()) {
            stopWatch.stop();
            LOG.info("doGet(): Search took " + stopWatch + " " + query.getQueryString());
        }
    }
    
    private void performReloads(
            final Site site,
            final String reload){
        
        if( "all".equalsIgnoreCase(reload) ){
            final boolean cleaned = SiteConfiguration.remove(site);
            LOG.warn(cleaned + WARN_CONFIG_CLEANED + site);
        } 
        if( "all".equalsIgnoreCase(reload) || "tabs".equalsIgnoreCase(reload) ){
            final boolean cleaned = SearchTabFactory.remove(site);
            LOG.warn(cleaned + WARN_TABS_CLEANED + site);
        }
        if( "all".equalsIgnoreCase(reload) || "modes".equalsIgnoreCase(reload) ){
            final boolean cleaned = SearchModeFactory.remove(site);
            LOG.warn(cleaned + WARN_MODES_CLEANED + site);
        }
    }
    
    private boolean isEmptyQuery(
            final HttpServletRequest request,
            final HttpServletResponse response ) throws IOException{
        
        if (request.getParameter("q") == null) {
            
            String redir = request.getContextPath();
            if (redir == null) { 
                redir = "/"; 
            }
            if (!redir.endsWith("/")) {
                LOG.debug("doGet: Adding / to " + redir);

                redir += "/";
            }

            LOG.info("doGet(): Empty Query String redirect=" + redir);

            response.sendRedirect(redir);
            return true;
        }
        return false;
    }
    
    private void updateContentType(
            final Site site,
            final HttpServletResponse response ){
        
        // TODO. Any better way to do this. Sitemesh?
        if (site.getName().startsWith("mobil")) {
            response.setContentType("text/xml; charset=utf-8");
        } else {
            response.setContentType("text/html; charset=utf-8");
        }

        response.setCharacterEncoding("UTF-8"); // correct encoding
    }

}
