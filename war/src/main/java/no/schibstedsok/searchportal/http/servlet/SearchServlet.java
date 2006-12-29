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
 * @author <a href="mailto:mick@wever.org">Mck</a>
 * @version <tt>$Revision: 3829 $</tt>
 */
public final class SearchServlet extends HttpServlet {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");

    private static final String ERR_MISSING_TAB = "No existing implementation for tab ";
    private static final String ERR_MISSING_MODE = "No existing implementation for mode ";

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


        // TODO Break the method down. It is way too long for a controlling class.

        if (!isEmptyQuery(request, response)) {

            LOG.trace("doGet()");
            LOG.debug("Character encoding ="  + request.getCharacterEncoding());

            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final Site site = (Site) request.getAttribute(Site.NAME_KEY);
            // BaseContext providing SiteContext and ResourceContext.
            //  We need it casted as a SiteContext for the ResourceContext code to be happy.
            final SiteContext genericCxt = new SiteContext(){
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
            };

            FactoryReloads.performReloads(genericCxt, request.getParameter("reload"));

            updateContentType(site, response, request);

            // TODO Make the default tab a property defined in each skins's views.xml
            final String searchTabKey = null != request.getParameter("c") ? request.getParameter("c") : "d";

            final SearchTab searchTab = findSearchTab(genericCxt, searchTabKey);

            if (searchTab == null) {
                LOG.error(ERR_MISSING_TAB + searchTabKey);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

            }else{

                // If the rss is hidden, require a partnerId.
                // The security by obscurity has been somewhat improved by the
                // addition of rssPartnerId as a md5-protected parameter (MD5ProtectedParametersFilter).
                if (request.getParameter("output") != null && "rss".equals(request.getParameter("output"))
                        && searchTab.getRssHidden() && request.getParameter("rssPartnerId") == null) {

                    response.sendError(HttpServletResponse.SC_NOT_FOUND);

                }else{

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
                    request.setAttribute("text",
                            TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, genericCxt)));
                    request.setAttribute("channels",
                            Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, genericCxt)));

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
                    request.setAttribute("no.schibstedsok.Statistics", new StringBuffer());

                    try {

                        final RunningQuery.Context rqCxt = ContextWrapper.wrap(
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
                        );

                        final RunningQuery query = QueryFactory.getInstance().createQuery(rqCxt, request, response);

                        query.run();

                        stopWatch.stop();
                        LOG.info("Search took " + stopWatch + " " + query.getQueryString());
                        STATISTICS_LOG.info(
                            "<search-servlet>"
                                + "<query>" + query.getQueryString() + "</query>"
                                + "<time>" + stopWatch + "</time>"
                                + ((StringBuffer)request.getAttribute("no.schibstedsok.Statistics")).toString()
                            + "</search-servlet>");


                        checkFinn(request, response);

                    } catch (InterruptedException e) {
                        LOG.error("Task timed out");
                    }
                }
            }
        }

    }


    private boolean isEmptyQuery(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException{

        String redirect = null;
        final String qParam = request.getParameter("q");
        final String cParm = request.getParameter("c");

        if (qParam == null) {
            redirect = null != request.getContextPath()
                    ? request.getContextPath()
                    : "/";

        }else if (null != cParm && ("d".equals(cParm) || "g".equals(cParm)) ) {
            // Extra check for the Norwegian web search. Search with an empty query string
            // should return the first page.
            if (qParam.trim().length() == 0) {
                redirect = "/";
            }
        }

        if( null != redirect ){
            LOG.info("doGet(): Empty Query String redirect=" + redirect);
            response.sendRedirect(redirect);
        }
        return null != redirect;
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
                LOG.error(ex.getMessage(), ex);
            }
        } else if (request.getParameter("output") != null && request.getParameter("output").equals("savedecorator")) {
                String showid = request.getParameter("showId");
                String userAgent = request.getHeader("User-Agent");
                String fileName = ".vcs";
                String charset = "utf-8";
                if(userAgent.indexOf("Windows") != -1){
                	charset = "iso-8859-1";
                }else if(userAgent.indexOf("Mac OS X") != -1)
                   fileName=".ics";
                if(showid == null)
                   showid="";
                response.setContentType("text/calendar; charset=" +charset);
                response.setHeader("Content-Disposition","attachment;filename=sesam-tvsok-" +showid +fileName	);
            	
        } else { 
            response.setContentType("text/html; charset=utf-8");
        }
    }

    /*
     *  redirects to yellowinfopage if request is from finn.no -> req.param("finn") = "finn"
     *  finn sends orgnumber as queryparam, if only 1 hit, then redirect.
     * @return true if a response.sendRedirect(..) was performed.
     */
    private boolean checkFinn(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException{

        if ("finn".equalsIgnoreCase(request.getParameter("finn"))) {

            final Map<String,Integer> hits = (Map<String,Integer>)request.getAttribute("hits");
            final Map<String,SearchResult> res = (Map<String,SearchResult>)request.getAttribute("results");

            final SearchResult sr = res.get("yellowPages");
            if (sr.getResults().size() > 0) {

                final SearchResultItem sri = sr.getResults().get(0);
                final String recordid = sri.getField("recordid").toString();
                final Integer yHits = hits.get("yellowPages");

                if (yHits == 1) {

                    final String url = "/search/?c=yip&q=" + request.getParameter("q")
                            + "&companyId=" + recordid
                            + "&companyId_x=" + new MD5Generator("S3SAM rockz").generateMD5(recordid)
                            + (null != request.getParameter("showtab")
                            ? "&showtab=" + request.getParameter("showtab")
                            : "");

                    LOG.info("Finn.no redirect: " + url);
                    response.sendRedirect(url);

                    return true;
                }
            }
        }
        return false;
    }

    private SearchTab findSearchTab(final BaseContext genericCxt, final String searchTabKey){

        SearchTab result = null;
        try{
            result = SearchTabFactory.valueOf(
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
        return result;
    }

}
