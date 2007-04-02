/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.http.servlet;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.SearchModeFactory;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import no.schibstedsok.searchportal.run.QueryFactory;
import no.schibstedsok.searchportal.run.RunningQuery;
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
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.http.servlet.FactoryReloads.ReloadArg;
import no.schibstedsok.searchportal.result.Linkpulse;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.util.TradeDoubler;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** The Central Controller to incoming queries.
 * Controls the SearchMode -> RunningQuery creation and handling.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchServlet extends HttpServlet {

   // Constants -----------------------------------------------------

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");

    private static final String ERR_MISSING_TAB = "No existing implementation for tab ";
    private static final String ERR_MISSING_MODE = "No existing implementation for mode ";


    // Attributes ----------------------------------------------------


    // Attributes ----------------------------------------------------
    //  Important that a Servlet does not have instance fields for synchronisation reasons.
    //

    // Static --------------------------------------------------------
    
    static{

        // when the root logger is set to DEBUG do not limit connection times
        if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
            System.setProperty("sun.net.client.defaultConnectTimeout", "1000");
            System.setProperty("sun.net.client.defaultReadTimeout", "3000");
        }
    }

    // Constructors --------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /** {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    // Package protected ---------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** {@inheritDoc}
     */
    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {


        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();
        final Site site = datamodel.getSite().getSite();

        // Anything that hasn't gone through the DataModelFilter isn't valid.
        //  redirects end up bouncing back in from the UrlRewriteFilter without ever reaching the client.
        if(null != parametersDO && null != site){

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

            if (!isEmptyQuery(datamodel, response, genericCxt)) {

                LOG.trace("doGet()");
                LOG.debug("Character encoding ="  + request.getCharacterEncoding());

                final StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                performFactoryReloads(request.getParameter("reload"), genericCxt);

                updateContentType(site, response, request);

                // determine the c parameter. default is 'd' unless there exists a page parameter when it becomes 'i'.
                final StringDataObject c = parametersDO.getValue("c");
                final StringDataObject page = parametersDO.getValue("page");
                final String searchTabKey = null != c &&  null != c.getString() && 0 < c.getString().length()
                        ? c.getString()
                        : null != page && null != page.getString() && 0 < page.getString().length() ? "i" : "d";

                final SearchTab searchTab = findSearchTab(genericCxt, searchTabKey);

                if (searchTab == null) {
                    LOG.error(ERR_MISSING_TAB + searchTabKey + " for site: " + site);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);

                }else{

                    // If the rss is hidden, require a partnerId.
                    // The security by obscurity has been somewhat improved by the
                    // addition of rssPartnerId as a md5-protected parameter (MD5ProtectedParametersFilter).
                    final StringDataObject output = parametersDO.getValue("output");
                    boolean hiddenRssWithoutPartnerId = null != output
                        && "rss".equals(output.getString())
                        && searchTab.isRssHidden()
                        && null == parametersDO.getValues().get("rssPartnerId");

                    if (hiddenRssWithoutPartnerId) {

                        response.sendError(HttpServletResponse.SC_NOT_FOUND);

                    }else{

                        performSearch(request, response, genericCxt, searchTab, stopWatch);

                    }
                }
            }
        }
    }

    // Private -------------------------------------------------------

    // Private -------------------------------------------------------

    private static boolean isEmptyQuery(
            final DataModel datamodel,
            final HttpServletResponse response,
            final SiteContext ctx) throws IOException{

        String redirect = null;
        final ParametersDataObject parametersDO = datamodel.getParameters();
        final Map<String,StringDataObject> params = parametersDO.getValues();
        final String qParam = null != params.get("q") ? params.get("q").getString() : "";
        final String cParm = null != params.get("c") ? params.get("c").getString() : "";
        final String wParam = null != params.get("where") ? params.get("where").getString() : "";

        // check if this is a sitesearch
        final Properties props = datamodel.getSite().getSiteConfiguration().getProperties();
        final boolean isSitesearch = Boolean.valueOf(props.getProperty(SiteConfiguration.IS_SITESEARCH_KEY));

        if (qParam == null) {
            redirect = null != parametersDO.getContextPath() && parametersDO.getContextPath().length() >0
                    ? parametersDO.getContextPath()
                    : "/";

        }else if (null != cParm && ("d".equals(cParm) || "g".equals(cParm) || "cat".equals(cParm) || "catip".equals(cParm)) && !isSitesearch) {
            // Extra check for the Norwegian web search. Search with an empty query string
            // should return the first page.
            if ("cat".equals(cParm) || "catip".equals(cParm)) {
                if (wParam.trim().length() == 0 && qParam.trim().length() == 0) {
                    redirect = "/";
                }                
            } else {
                if (qParam.trim().length() == 0) {
                    redirect = "/";
                }
            }
        }

        if( null != redirect ){
            LOG.info("doGet(): Empty Query String redirect=" + redirect);
            response.sendRedirect(redirect);
        }
        return null != redirect;    
    }

    private static void performFactoryReloads(
            final String reload,
            final SiteContext genericCxt){

        if( null != reload && reload.length() >0 ){
            try{
                final ReloadArg arg = ReloadArg.valueOf(reload.toUpperCase());
                FactoryReloads.performReloads(genericCxt, arg);

            }catch(IllegalArgumentException ex){
                LOG.info("Invalid reload parameter -->" + reload);
            }
        }
    }

    private static void updateContentType(
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
        } else if (request.getParameter ("output") != null && request.getParameter ("output").equals ("savedecorator")) {
            String showid = request.getParameter ("showId");
            String userAgent = request.getHeader ("User-Agent");
            String fileName = ".vcs";
            String charset = "utf-8";
            if(userAgent.indexOf ("Windows") != -1){
                charset = "iso-8859-1";
            }else if(userAgent.indexOf ("Mac OS X") != -1)
                fileName=".ics";
            if(showid == null)
                showid="";
            response.setContentType ("text/calendar; charset=" +charset);
            response.setHeader ("Content-Disposition","attachment;filename=sesam-tvsok-" +showid +fileName	);
            
        } else if (request.getParameter ("output") != null && request.getParameter ("output").equals ("vcarddecorator")) {
            String showid = request.getParameter ("showId");
            String userAgent = request.getHeader ("User-Agent");
            String fileName = ".vcf";
            String charset = "utf-8";
            if(userAgent.indexOf ("Windows") != -1)
                charset = "iso-8859-1";
            if(showid == null)
                showid="";
            response.setCharacterEncoding (charset);
            response.setContentType ("text/x-vcard; charset=" +charset);
            response.setHeader ("Content-Disposition","attachment;filename=vcard-" +showid + ".vcf");
        } else if (request.getParameter ("output") != null
                && (request.getParameter ("output").equals ("opensearch") || request.getParameter ("output").equals ("xml"))) {
            String charset = "utf-8";
            response.setCharacterEncoding (charset);
            response.setContentType ("text/xml; charset=utf-8");
        } else {
            response.setContentType ("text/html; charset=utf-8");
        }
    }

    private static void updateAttributes(
            final HttpServletRequest request,
            final RunningQuery.Context rqCxt){


        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();

        if (null == parametersDO.getValue("offset") || 0 == parametersDO.getValue("offset").getString().length() ) {
            request.setAttribute("offset", "0"); // TODO remove, access through datamodel instead.
        }

         // TODO remove next three, access through datamodel instead.
        request.setAttribute("tradedoubler", new TradeDoubler(request));
        request.setAttribute("text",TextMessages.valueOf(ContextWrapper.wrap(
                TextMessages.Context.class, 
                rqCxt,new SiteContext(){
                public Site getSite() {
                    return datamodel.getSite().getSite();
                }
        })));
        request.setAttribute("no.schibstedsok.Statistics", new StringBuffer());

        final Properties props = datamodel.getSite().getSiteConfiguration().getProperties();

        request.setAttribute("linkpulse", new Linkpulse(datamodel.getSite().getSite(), props));
    }

    /* TODO Move into a RunningQueryHandler
     *
     *  redirects to yellowinfopage if request is from finn.no -> req.param("finn") = "finn"
     *  finn sends orgnumber as queryparam, if only 1 hit, then redirect.
     * @return true if a response.sendRedirect(..) was performed.
     */

    /* TODO Move into a RunningQueryHandler
     *
     *  redirects to yellowinfopage if request is from finn.no -> req.param("finn") = "finn"
     *  finn sends orgnumber as queryparam, if only 1 hit, then redirect.
     * @return true if a response.sendRedirect(..) was performed.
     */
    private static boolean checkFinn(
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

    private static SearchTab findSearchTab(final BaseContext genericCxt, final String searchTabKey){

        SearchTab result = null;
        try{
            result = SearchTabFactory.valueOf(
                ContextWrapper.wrap(
                    SearchTabFactory.Context.class,
                    genericCxt)).getTabByKey(searchTabKey);

        }catch(AssertionError ae){
            // it's not normal to catch assert errors but we really want a 404 not 500 response error.
            LOG.error("Caught Assertion: " + ae);
        }
        return result;
    }

    private void performSearch(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final SiteContext genericCxt,
            final SearchTab searchTab,
            final StopWatch stopWatch) throws IOException{

        final SearchMode mode = SearchModeFactory.valueOf(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());

        if (mode == null) {
            LOG.error(ERR_MISSING_MODE + searchTab.getMode());
            throw new UnsupportedOperationException(ERR_MISSING_MODE + searchTab.getMode());
        }
        
        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final StringDataObject output = datamodel.getParameters().getValue("output");

        final RunningQuery.Context rqCxt = ContextWrapper.wrap(
                RunningQuery.Context.class,
                new BaseContext() {
                    public DataModel getDataModel(){
                        return datamodel;
                    }
                    public SearchMode getSearchMode() {
                        return mode;
                    }
                    public SearchTab getSearchTab() {
                        return searchTab;
                    }
                },
                genericCxt
        );

        updateAttributes(request, rqCxt);

        if(null == output || !"opensearch".equalsIgnoreCase(output.getString())){
            
            try {

                final RunningQuery query = QueryFactory.getInstance().createQuery(rqCxt, request, response);

                query.run();

                stopWatch.stop();
                LOG.info("Search took " + stopWatch + " " + datamodel.getQuery().getString());

                if(!"NOCOUNT".equals(request.getParameter("IGNORE"))){

                    STATISTICS_LOG.info(
                        "<search-servlet"
                            + (null != output ? " output=\"" + output.getXmlEscaped() + "\">" : ">")
                            + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>"
                            + "<time>" + stopWatch + "</time>"
                            + ((StringBuffer)request.getAttribute("no.schibstedsok.Statistics")).toString()
                        + "</search-servlet>");
                }


                checkFinn(request, response);

            } catch (InterruptedException e) {
                LOG.error("Task timed out");
            } catch (SiteKeyedFactoryInstantiationException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    // Inner classes -------------------------------------------------

}
