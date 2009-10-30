/* Copyright (2006-2009) Schibsted ASA
 * This file is part of SESAT.
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
 *
 * DataModelFilter.java
 *
 * Created on 26 January 2007, 22:29
 *
 */

package no.sesat.search.http.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.DataModelUtility;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.UrlResourceLoader;
import no.sesat.search.view.config.SearchTab;
import org.apache.log4j.Logger;

/** Ensures that a session is created, and that a new DataModel, with Site and Browser dataObjects,
 * exists within it.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class DataModelFilter implements Filter {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelFilter.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of DataModelFilter */
    public DataModelFilter() {
    }



    // Public --------------------------------------------------------

    @Override
    public void init(final FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain)
                throws IOException, ServletException {

        if(request instanceof HttpServletRequest){
            final HttpServletRequest httpRequest = (HttpServletRequest)request;

            final Site site = (Site) httpRequest.getAttribute(Site.NAME_KEY);

            final DataModelFactory factory;
            try{
                factory = DataModelFactory.instanceOf(new DataModelFactory.Context(){
                    @Override
                    public Site getSite() {
                        return site;
                    }
                    @Override
                    public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                                final String resource,
                                                                final Properties properties) {
                        return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                    }
                });

            }catch(SiteKeyedFactoryInstantiationException skfie){
                LOG.error(skfie.getMessage(), skfie);
                throw new ServletException(skfie.getMessage(), skfie);
            }

            final DataModel datamodel = getDataModel(factory, httpRequest);
            try{

                final ParametersDataObject parametersDO = updateDataModelForRequest(factory, datamodel, httpRequest);

                datamodel.setParameters(parametersDO);

                if(null == datamodel.getSite() || !datamodel.getSite().getSite().equals(site)){
                    datamodel.setSite(DataModelUtility.getSiteDO(
                            factory,
                            datamodel,
                            (Site) request.getAttribute(Site.NAME_KEY),
                            (SiteConfiguration) request.getAttribute(SiteConfiguration.NAME_KEY)));
                }

                // DataModel's ControlLevel will be REQUEST_CONSTRUCTION (from getDataModel(..))
                //  Increment it onwards to VIEW_CONSTRUCTION.
                // SearchServlet will assign it back to REQUEST_CONSTRUCTION if neccessary.
                factory.assignControlLevel(datamodel, ControlLevel.VIEW_CONSTRUCTION);

                chain.doFilter(request, response);

            }finally{
                cleanDataModel(factory, datamodel);
            }


        }else{
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private static DataModel getDataModel(final DataModelFactory factory, final HttpServletRequest request){

        final HttpSession session = request.getSession();

        DataModel datamodel = (DataModel) session.getAttribute(DataModel.KEY);

        if(null == datamodel){
            datamodel = createDataModel(factory, request);
            session.setAttribute(DataModel.KEY, datamodel);
        }

        // DataModel's ControlLevel will be DATA_MODEL_CONSTRUCTION or VIEW_CONSTRUCTION (from the past request)
        //  Increment it onwards to REQUEST_CONSTRUCTION.
        return factory.assignControlLevel(datamodel, ControlLevel.REQUEST_CONSTRUCTION);
    }

    private static DataModel createDataModel(final DataModelFactory factory, final HttpServletRequest request){

        final List<Locale> locales = new ArrayList<Locale>();
        for(@SuppressWarnings("unchecked") Enumeration<Locale> en = request.getLocales(); en.hasMoreElements();){
            locales.add(en.nextElement());
        }

        return DataModelUtility.createDataModel(
                factory,
                (Site) request.getAttribute(Site.NAME_KEY),
                (SiteConfiguration) request.getAttribute(SiteConfiguration.NAME_KEY),
                request.getHeader("User-Agent"),
                (String) request.getAttribute("REMOTE_ADDR"),
                request.getHeader("x-forwarded-for"),
                request.getLocale(),
                locales);
    }

    /** Update the request elements in the datamodel.
     * TODO public documentation on "-isUrl" and "-isCookie" parameter key suffixes.
     **/
    private static ParametersDataObject updateDataModelForRequest(
            final DataModelFactory factory,
            final DataModel datamodel,
            final HttpServletRequest request){

        final Map<String,Map<String,String>> paramMaps = new HashMap<String,Map<String,String>>();
        try{
            final Map<String,String> urlParameters = new HashMap<String,String>();
            paramMaps.put("Url", urlParameters);
            for(@SuppressWarnings("unchecked")
                    Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ){

                final String key = e.nextElement();
                urlParameters.put(key, getParameterSafely(request, key));
            }
            if (null != request.getCookies()) {
                final Map<String,String> cookieParameters = new HashMap<String,String>();
                paramMaps.put("Cookie", cookieParameters);
                for(Cookie cookie : request.getCookies()){
                    cookieParameters.put(cookie.getName(), cookie.getValue());
                }
            }

        }catch(Exception e){
            // maybe the client disconnected.
            LOG.warn("failed to read parameters/cookies: " + e.getMessage());
        }

        return DataModelUtility.updateDataModelForRequest(
                factory,
                datamodel,
                request.getContextPath(),
                paramMaps,
                SiteLocatorFilter.getRequestId(request));
    }

    /** Clean out everything in the datamodel that is not flagged to be long-lived. **/
    @SuppressWarnings("deprecation")
    private static void cleanDataModel(final DataModelFactory factory, final DataModel datamodel){

        if(null != datamodel.getPage() && null != datamodel.getPage().getCurrentTab()){

            final SearchTab.Scope scope = datamodel.getPage().getCurrentTab().getScope();
            // we need to reset control level so to be able to unset properties in it.
            factory.assignControlLevel(datamodel, ControlLevel.DATA_MODEL_CONSTRUCTION);

            for(String key : datamodel.getJunkYard().getValues().keySet()){
                datamodel.getJunkYard().setValue(key, null);
            }

            if(SearchTab.Scope.REQUEST == scope){
                for(String key : datamodel.getSearches().keySet()){
                    datamodel.setSearch(key, null);
                }
                if(null != datamodel.getNavigation() && null != datamodel.getNavigation().getNavigations()){
                    for(String key : datamodel.getNavigation().getNavigations().keySet()){
                        datamodel.getNavigation().setNavigation(key, null);
                    }
                }
                datamodel.setNavigation(null);
                datamodel.setParameters(null);
                datamodel.setQuery(null);
                datamodel.getPage().setCurrentTab(null);
            }
        }

        assert isSerializable(datamodel) : "Datamodel is not serializable!";
        factory.assignControlLevel(datamodel, ControlLevel.VIEW_CONSTRUCTION);
    }

    private static boolean isSerializable(final DataModel datamodel) {

        boolean retval = false;

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(baos);

            LOG.info("Serializing datamodel");
            os.writeObject(datamodel);
            retval = true;

        } catch (NotSerializableException e) {
            LOG.error("Failed to serialize datamodel: " + e);
        } catch (IOException e) {
            LOG.error("Failed to serialize datamodel:" + e);
        }

        return retval;
    }

    /**
     * This function will try to decode the raw parameter, and see if that matches
     * how the request.getParameter(..) did the decoding. If this dosn't match then we
     * fall back to ISO-8859-1 which in most cases will be correct.
     *
     * @param request The servlet request we are processing
     * @param parameter The parameter to retrieve
     * @return The correct decoded parameter
     */
    private static String getParameterSafely(final HttpServletRequest request, final String parameter){

        final StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
        String value = request.getParameter(parameter);
        String queryStringValue = null;

        final String parameterEquals = parameter + '=';
        while(st.hasMoreTokens()) {
            final String tmp = st.nextToken();
            if (tmp.startsWith(parameterEquals)) {
                queryStringValue = tmp.substring(parameterEquals.length());
                break;
            }
        }

        if (null != value && null != queryStringValue) {
            try {
                final String queryStringValueDecoded = URLDecoder.decode(queryStringValue, "UTF-8");
                if (!queryStringValueDecoded.equals(value) || value.contains("\ufffd")) {// also check for UTF-8 unknown
                    // We don't think the encoding is utf-8 so go for ISO-8859-1
                    value = URLDecoder.decode(queryStringValue, "ISO-8859-1");
                }
            } catch (UnsupportedEncodingException e) {
                LOG.trace(e);
            }
        }

        return value;
    }

    // Inner classes -------------------------------------------------

}
