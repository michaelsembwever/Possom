/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.http.servlet;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.SearchModeFactory;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.util.Channels;
import no.schibstedsok.searchportal.util.QueryStringHelper;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import no.schibstedsok.searchportal.run.QueryFactory;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import no.schibstedsok.searchportal.security.MD5Generator;
import org.apache.commons.lang.time.StopWatch;

import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import no.schibstedsok.searchportal.result.Linkpulse;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.util.TradeDoubler;
import org.apache.log4j.Logger;

/** The Central Controller to incoming queries.
 * Controls the SearchMode -> RunningQuery creation and handling.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3829 $</tt>
 */
public final class SearchServlet extends HttpServlet {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");
    
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

        LOG.trace("ENTR: doGet()");
        LOG.debug("Character encoding ="  + request.getCharacterEncoding());

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final Site site = (Site) request.getAttribute(Site.NAME_KEY);
        // BaseContext providing SiteContext and ResourceContext.
        //  We need it casted as a SiteContext for the ResourceContext code to be happy.
        final SiteContext genericCxt = new SiteContext(){// <editor-fold defaultstate="collapsed" desc=" genericCxt ">
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final Properties properties) {
                
                return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public DocumentLoader newDocumentLoader(
                    final SiteContext siteCxt,
                    final String resource, 
                    final DocumentBuilder builder) {
                
                return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
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
        
        SearchTab st = null;
        try{
            st = SearchTabFactory.valueOf(
                ContextWrapper.wrap(
                    SearchTabFactory.Context.class, 
                    genericCxt,
                    new BaseContext(){
                        public SearchTabFactory getLeafSearchTabFactory(){
                            return null;
                        }
                    })).getTabByKey(searchTabKey);
            
        }catch(AssertionError ae){
            // it's not normal to catch assert errors but we really want a 404 not 500 response error.
            LOG.error("Caught Assertion: " + ae);
        }
        final SearchTab searchTab = st;
        
        if (searchTab == null) {
            LOG.error(ERR_MISSING_TAB + searchTabKey);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final SearchMode mode = SearchModeFactory.valueOf(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());

        if (mode == null) {
            LOG.error(ERR_MISSING_MODE + searchTab.getMode());
            throw new UnsupportedOperationException(ERR_MISSING_MODE + searchTab.getMode());
        }

        final Properties props = SiteConfiguration.valueOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class, genericCxt)).getProperties();
        request.setAttribute("configuration", props);
        request.setAttribute("text", TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, genericCxt)));
//        request.setAttribute("channels", Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, genericCxt)));

        if (request.getParameter("offset") == null || "".equals(request.getParameter("offset"))) {
            request.setAttribute("offset", "0");
        }

        if (request.getParameter("q") != null) {
            request.setAttribute("q", QueryStringHelper.safeGetParameter(request, "q"));
        }

        request.setAttribute("tab", searchTab);
        request.setAttribute("c", searchTabKey);
        request.setAttribute("contextPath", request.getContextPath());
        request.setAttribute("linkpulse", new Linkpulse(site, props));
        request.setAttribute("tradedoubler", new TradeDoubler(request));

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

            query.run();

            stopWatch.stop();
            LOG.info("Search took " + stopWatch + " " + query.getQueryString());
            STATISTICS_LOG.info(
                "<search-servlet>"
                    + "<query>" + query.getQueryString() + "</query>"
                    + "<time>" + stopWatch + "</time>"
                + "</search-servlet>");


            if ("finn".equals(request.getParameter("finn"))) {
                if (checkFinn(request, response)) {
                    return;
                }            
            }
            
            if (isEmptyQuery(request, response)) {
                return;
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
        if (request.getParameter("c") != null && (request.getParameter("c").equals("d") || request.getParameter("c").equals("g")) ) {
            if (request.getParameter("q").trim().length() == 0) {
                LOG.info("doGet(): Empty Query String redirect=/");
                response.sendRedirect("/");
                return true;
            }
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
        } else if (site.getName().startsWith("mobil") || site.getName().startsWith("xml")) {
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
    
    /* 
     *  redirects to yellowinfopage if request is from finn.no -> req.param("finn") = "finn" 
     *  finn sends orgnumber as queryparam, if only 1 hit, then redirect.
     */
    private boolean checkFinn(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException{

        Map<String,Integer> hits = (Map<String,Integer>)request.getAttribute("hits");
        Map<String,SearchResult> res = (Map<String,SearchResult>)request.getAttribute("results");
        SearchResult sr = res.get("yellowPages");
        if (sr.getResults().size() == 0) {
            return false;
        }
        SearchResultItem sri = sr.getResults().get(0);
        String recordid = sri.getField("recordid").toString();
        Integer yHits = hits.get("yellowPages");

        if (yHits == 1) {
            final MD5Generator md5 = new MD5Generator("S3SAM rockz");            
            String showtab = "";
            if (request.getParameter("showtab") != null)    
                showtab = "&showtab=" + request.getParameter("showtab");

            String url = "/search/?c=yip&q=" + request.getParameter("q") + "&companyId=" + recordid + "&companyId_x=" + md5.generateMD5(recordid) + showtab;
            LOG.info("doGet(): Finn.no redirect: " + url);
            response.sendRedirect(url);
            return true;
        } 
        
        return false;
    }
}
