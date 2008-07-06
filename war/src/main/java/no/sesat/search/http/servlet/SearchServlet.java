/*
 * Copyright (2005-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.http.servlet;


import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.page.PageDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.http.servlet.FactoryReloads.ReloadArg;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.mode.SearchModeFactory;
import no.sesat.search.run.QueryFactory;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.TextMessages;
import no.sesat.search.site.config.UrlResourceLoader;
import no.sesat.search.view.SearchTabFactory;
import no.sesat.search.view.config.SearchTab;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/** The Central Controller to incoming queries.
 * Controls the SearchMode -> RunningQuery creation and handling.
 *
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class SearchServlet extends  HttpServlet {

   // Constants -----------------------------------------------------

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;
    private static final String REMOTE_ADDRESS_KEY = "REMOTE_ADDR";

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);
    private static final Logger ACCESS_LOG = Logger.getLogger("no.sesat.Access");
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.sesat.Statistics");

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
            System.setProperty("http.keepAlive", "false");
            System.setProperty("sun.net.client.defaultConnectTimeout", "1000");
            System.setProperty("sun.net.client.defaultReadTimeout", "3000");
            System.setProperty("sun.net.http.errorstream.enableBuffering", "true");
        }
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /** {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {

        logAccessRequest(request);
        LOG.trace("doGet()");

        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();
        final Site site = datamodel.getSite().getSite();

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

            public BytecodeLoader newBytecodeLoader(SiteContext context, String className, final String jar) {
                return UrlResourceLoader.newBytecodeLoader(context, className, jar);
            }

            public Site getSite() {
                return site;
            }
        };

        final DataModelFactory dmFactory;
        try{
            dmFactory = DataModelFactory.instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, genericCxt));

        }catch(SiteKeyedFactoryInstantiationException skfe){
            throw new ServletException(skfe);
        }

        // DataModel's ControlLevel will be VIEW_CONSTRUCTION (safe setting set by DataModelFilter)
        //  Bring it back to VIEW_CONSTRUCTION.
        dmFactory.assignControlLevel(datamodel, ControlLevel.REQUEST_CONSTRUCTION);

        try{

            final String cParameter = null != parametersDO.getValue(SearchTab.PARAMETER_KEY)
                    ? parametersDO.getValue(SearchTab.PARAMETER_KEY).getString()
                    : null;

            final SearchTab searchTab = updateSearchTab(cParameter, request, dmFactory, genericCxt);

            if (null!= searchTab) {
                LOG.debug("Character encoding ="  + request.getCharacterEncoding());

                final StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                final String ipAddr = null != request.getAttribute(REMOTE_ADDRESS_KEY)
                    ? (String) request.getAttribute(REMOTE_ADDRESS_KEY)
                    : request.getRemoteAddr();

                performFactoryReloads(
                        request.getParameter("reload"),
                        genericCxt,
                        ipAddr,
                        datamodel.getSite().getSiteConfiguration());

                // If the rss is hidden, require a partnerId.
                // The security by obscurity has been somewhat improved by the
                // addition of rssPartnerId as a md5-protected parameter (MD5ProtectedParametersFilter).
                final StringDataObject layout = parametersDO.getValue("layout");
                boolean hiddenRssWithoutPartnerId = null != layout
                    && "rss".equals(layout.getString())
                    && searchTab.isRssHidden()
                    && null == parametersDO.getValues().get("rssPartnerId");

                if (hiddenRssWithoutPartnerId) {

                    response.sendError(HttpServletResponse.SC_NOT_FOUND);

                } else if (null != layout && "rss".equals(layout.getString()) && "".equals(searchTab.getRssResultName())) {

                    LOG.warn("RSS not supported for requested vertical " + searchTab.toString());
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);

                } else {
                    performSearch(request, response, genericCxt, searchTab, stopWatch);
                }

            }else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/start.jsp");
            dispatcher.forward(request, response);

        }finally{

            // DataModel's ControlLevel will be REQUEST_CONSTRUCTION or RUNNING_QUERY_HANDLING
            //  Increment it onwards to VIEW_CONSTRUCTION.
            dmFactory.assignControlLevel(datamodel, ControlLevel.VIEW_CONSTRUCTION);
        }
    }

    // Private -------------------------------------------------------


    private static void performFactoryReloads(
            final String reload,
            final SiteContext genericCxt,
            final String ipAddr,
            final SiteConfiguration siteConf){

        // check if user ipaddress is permitted to perform a factory reload.
        boolean allowed =
                ipAddr.startsWith("127.") || ipAddr.startsWith("10.") || ipAddr.startsWith("0:0:0:0:0:0:0:1%0");

        final String[] ipaddress = null != siteConf.getProperty("sesat.factoryReload.ipaddresses.allowed")
                ? siteConf.getProperty("sesat.factoryReload.ipaddresses.allowed").split(",")
                : new String[]{};

        for (String s : ipaddress) { allowed |= ipAddr.startsWith(s); }

        if (null != reload && reload.length() > 0){

            if (allowed){
                try{
                    final ReloadArg arg = ReloadArg.valueOf(reload.toUpperCase());
                    FactoryReloads.performReloads(genericCxt, arg);

                }catch(IllegalArgumentException ex){
                    LOG.info("Invalid reload parameter -->" + reload);
                }
            }else{
                LOG.warn("ipaddress " + ipAddr + " not allowed to performFactoryReload(..)");
            }
        }
    }

    private static void updateAttributes(
            final HttpServletRequest request,
            final RunningQuery.Context rqCxt){


        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);

         // TODO remove next two, access through datamodel instead.
        request.setAttribute("text",TextMessages.valueOf(ContextWrapper.wrap(
                TextMessages.Context.class,
                rqCxt,new SiteContext(){
                public Site getSite() {
                    return datamodel.getSite().getSite();
                }
        })));
        request.setAttribute("no.sesat.Statistics", new StringBuffer());

    }

    private static SearchTab updateSearchTab(
            final String cParameter,
            final HttpServletRequest request,
            final DataModelFactory dmFactory,
            final BaseContext genericCxt){

        // determine the c parameter.
        //  default comes from SiteConfiguration unless there exists a page parameter when it becomes 'i'.
        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();
        final StringDataObject page = parametersDO.getValue("page");
        final String defaultSearchTabKey
                = datamodel.getSite().getSiteConfiguration().getProperty(SiteConfiguration.DEFAULTTAB_KEY);

        final String searchTabKey = null != cParameter && 0 < cParameter.length()
                ? cParameter
                : null != page && null != page.getString() && 0 < page.getString().length() ? "i" : defaultSearchTabKey;

        LOG.info("searchTabKey:" +searchTabKey);

        SearchTab result = null;
        try{
            final SearchTabFactory stFactory = SearchTabFactory.instanceOf(
                ContextWrapper.wrap(
                    SearchTabFactory.Context.class,
                    genericCxt));

            result = stFactory.getTabByKey(searchTabKey);

            if(null == datamodel.getPage()){

                final PageDataObject pageDO = dmFactory.instantiate(
                        PageDataObject.class,
                        datamodel,
                        new DataObject.Property("tabs", stFactory.getTabsByName()),
                        new DataObject.Property("currentTab", result));

                datamodel.setPage(pageDO);
            }else{
                datamodel.getPage().setCurrentTab(result);
            }

            // this is legacy. shorter to write in templates than $datamodel.page.currentTab
            request.setAttribute("tab", datamodel.getPage().getCurrentTab());

        }catch(AssertionError ae){
            // it's not normal to catch assert errors but we really want a 404 not 500 response error.
            LOG.error("Caught Assertion: " + ae);
        }
        if(null==result){
            LOG.error(ERR_MISSING_TAB + searchTabKey);
            // first going to fallback to defaultSearchTabKey in preference to the pending 404 response error.
            if(!defaultSearchTabKey.equals(searchTabKey)){
                //parametersDO.setValue(SearchTab.PARAMETER_KEY, new StringDataObjectSupport(defaultSearchTabKey));
                result = updateSearchTab(defaultSearchTabKey, request, dmFactory, genericCxt);
            }
        }
        return result;
    }

    private static void performSearch(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final SiteContext genericCxt,
            final SearchTab searchTab,
            final StopWatch stopWatch) throws IOException{

        final SearchMode mode = SearchModeFactory.instanceOf(
                ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt))
                .getMode(searchTab.getMode());

        if (mode == null) {
            LOG.error(ERR_MISSING_MODE + searchTab.getMode());
            throw new UnsupportedOperationException(ERR_MISSING_MODE + searchTab.getMode());
        }

        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final StringDataObject layout = datamodel.getParameters().getValue("layout");

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

        if(null == layout || !"opensearch".equalsIgnoreCase(layout.getString())){

            try {

                // DataModel's ControlLevel will be REQUEST_CONSTRUCTION
                //  Increment it onwards to RUNNING_QUERY_CONSTRUCTION.
                DataModelFactory
                        .instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, genericCxt))
                        .assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_CONSTRUCTION);

                final RunningQuery query = QueryFactory.getInstance().createQuery(rqCxt, request, response);

                if( !datamodel.getQuery().getQuery().isBlank() || searchTab.isExecuteOnBlank() ){

                    query.run();
                    stopWatch.stop();
                    LOG.info("Search took " + stopWatch + " " + datamodel.getQuery().getString());

                    if(!"NOCOUNT".equals(request.getParameter("IGNORE"))){

                        STATISTICS_LOG.info(
                            "<search-servlet"
                                + (null != layout ? " layout=\"" + layout.getXmlEscaped() + "\">" : ">")
                                + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>"
                                + "<time>" + stopWatch + "</time>"
                                + ((StringBuffer)request.getAttribute("no.sesat.Statistics")).toString()
                            + "</search-servlet>");
                    }
                }

            } catch (InterruptedException e) {
                LOG.error("Task timed out");
            } catch (SiteKeyedFactoryInstantiationException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    // next to duplicate from SiteLocatorFilter
    private static void logAccessRequest(final HttpServletRequest request){

        final String url = request.getRequestURI()
                + (null != request.getQueryString() ? '?' + request.getQueryString() : "");
        final String referer = request.getHeader("Referer");
        final String method = request.getMethod();
        final String ip = request.getRemoteAddr();
        final String userAgent = request.getHeader("User-Agent");
        final String sesamId = getCookieValue(request, "SesamID");
        final String sesamUser = getCookieValue(request, "SesamUser");

        ACCESS_LOG.info("<search-servlet>"
                + "<real-url method=\"" + method + "\">" + StringEscapeUtils.escapeXml(url) + "</real-url>"
                + (null != referer ? "<referer>" + StringEscapeUtils.escapeXml(referer) + "</referer>" : "")
                + "<browser ipaddress=\"" + ip + "\">" + StringEscapeUtils.escapeXml(userAgent) + "</browser>"
                + "<user id=\"" + sesamId + "\">" + sesamUser + "</user>"
                + "</search-servlet>");
    }

    // probably apache commons could simplify this // duplicated in SiteLocatorFilter
    static String getCookieValue(final HttpServletRequest request, final String cookieName){

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

    // Inner classes -------------------------------------------------

}
