/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
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
import no.schibstedsok.front.searchportal.util.Channels;
import no.schibstedsok.front.searchportal.util.QueryStringHelper;
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


        if (isEmptyQuery(request, response)) {
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


        FactoryReloads.performReloads(genericCxt, request.getParameter("reload"));

        updateContentType(site, response, request);

        String searchTabKey = request.getParameter("c");

        if (searchTabKey == null) {
            searchTabKey = "d";
        }

        final SearchTab searchTab = SearchTabFactory.valueOf(
            ContextWrapper.wrap(SearchTabFactory.Context.class, genericCxt)).getTabByKey(searchTabKey);

        if (searchTab == null) {
            LOG.error(ERR_MISSING_TAB + searchTabKey);
            throw new UnsupportedOperationException(ERR_MISSING_TAB + searchTabKey);
        }

        final SearchMode mode = SearchModeFactory.valueOf(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());

        if (mode == null) {
            LOG.error(ERR_MISSING_MODE + searchTab.getMode());
            throw new UnsupportedOperationException(ERR_MISSING_MODE + searchTab.getMode());
        }

        request.setAttribute("text", TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, genericCxt)));
        request.setAttribute("channels", Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, genericCxt)));

        if (request.getParameter("offset") == null || "".equals(request.getParameter("offset"))) {
            request.setAttribute("offset", "0");
        }

        if (request.getParameter("q") != null) {
            request.setAttribute("q", QueryStringHelper.safeGetParameter(request, "q"));
        }

        request.setAttribute("tab", searchTab);
        request.setAttribute("c", searchTabKey);

        try {

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

            final RunningQuery query = QueryFactory.getInstance().createQuery(rqCxt, request, response);

            request.setAttribute("locale", query.getLocale());
            request.setAttribute("query", query);

            query.run();

            if (LOG.isInfoEnabled()) {
                stopWatch.stop();
                LOG.info("doGet(): Search took " + stopWatch + " " + query.getQueryString());
            }

        } catch (InterruptedException e) {
            LOG.error("Task timed out");
        }


    }


    private boolean isEmptyQuery(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException{

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

        // Extra check for the Norwegian web search. Search with an empty query string
        // should return the first page.
        if (request.getParameter("c") != null && request.getParameter("c").equals("d")) {
            if (request.getParameter("q").trim().length() == 0) {
                response.sendRedirect("/");
            }
            return true;
        }

        return false;
    }

    private void updateContentType(
            final Site site,
            final HttpServletResponse response,
            final HttpServletRequest request){

        /* Setting default encoding */
        response.setCharacterEncoding("UTF-8");

        // TODO. Any better way to do this. Sitemesh?
        if (request.getParameter("output") != null && request.getParameter("output").equals("rss")) {
            if (request.getParameter("encoding") != null && request.getParameter("encoding").equals("iso-8859-1")){
                response.setContentType("text/xml; charset=iso-8859-1");
                response.setCharacterEncoding("iso-8859-1"); // correct encoding
            } else {
                response.setContentType("text/xml; charset=utf-8");
            }
        } else if (site.getName().startsWith("mobil")) {
            response.setContentType("text/xml; charset=utf-8");
            try {
                // Just can't get sitemesh to work in the way I imagine it works.
                response.getWriter().write(
                    "<html><head><META name=\"decorator\" content=\"mobiledecorator\"/></head></html>");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            response.setContentType("text/html; charset=utf-8");
        }
    }
}
