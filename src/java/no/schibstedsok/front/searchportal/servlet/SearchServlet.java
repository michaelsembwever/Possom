// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.servlet;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchModeFactory;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import no.schibstedsok.front.searchportal.util.QueryStringHelper;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.query.run.QueryFactory;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
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
    private static final String WARN_TABS_CLEANED = " status on cleaning tabs for ";

    //private SearchTabs tabs;

    /** {@inheritDoc}
     */
    public void destroy() {
        //tabs.stopAll();
    }

    /** {@inheritDoc}
     */
    protected void doGet(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse)
                throws ServletException, IOException {


        if (httpServletRequest.getParameter("q") == null) {
            String redir = httpServletRequest.getContextPath();
            if (redir == null) { redir = "/"; }
            if (!redir.endsWith("/")) {
                LOG.debug("doGet: Adding / to " + redir);

                redir += "/";
            }

            LOG.info("doGet(): Empty Query String redirect=" + redir);

            httpServletResponse.sendRedirect(redir);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: doGet()");
            LOG.debug("Character encoding ="  + httpServletRequest.getCharacterEncoding());
        }

        StopWatch stopWatch = null;
        if (LOG.isInfoEnabled()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        final Site site = (Site) httpServletRequest.getAttribute(Site.NAME_KEY);
        final boolean forceReload = "tabs".equals(httpServletRequest.getParameter("reload"));

        if( forceReload ){
            final boolean cleaned = SiteConfiguration.remove(site);
            LOG.warn(cleaned + WARN_TABS_CLEANED + site);
        }
        //final SearchTabs tabs = SiteConfiguration.valueOf(site).getSearchTabs();

        final String xmlParam = httpServletRequest.getParameter("xml");

        if (xmlParam != null && xmlParam.equals("yes")) {
            httpServletResponse.setContentType("text/xml; charset=utf-8");
        } else {
            httpServletResponse.setContentType("text/html; charset=utf-8");
        }
        httpServletResponse.setCharacterEncoding("UTF-8"); // correct encoding

        String searchTabKey = httpServletRequest.getParameter("c");

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
        final SearchMode mode = SearchModeFactory.getModeFactory(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());
        
        //final SearchMode mode = tabs.getSearchMode(searchTabKey);

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
            .createQuery(rqCxt, httpServletRequest, httpServletResponse);

        httpServletRequest.setAttribute("locale", query.getLocale());
        httpServletRequest.setAttribute("query", query);
        httpServletRequest.setAttribute("site", site);
        httpServletRequest.setAttribute("text", 
                TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, genericCxt)));

        if (httpServletRequest.getParameter("offset") != null
                && !"".equals(httpServletRequest.getParameter("offset"))) {

            query.setOffset(Integer.parseInt(httpServletRequest.getParameter("offset")));
        }

        if (httpServletRequest.getParameter("q") != null) {
            httpServletRequest.setAttribute("q", 
                QueryStringHelper.safeGetParameter(httpServletRequest, "q"));
        }

        httpServletRequest.setAttribute("tab", searchTab);
        httpServletRequest.setAttribute("c", searchTabKey);

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

}
