/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

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
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.junkyard.JunkYardDataObject;
import no.sesat.search.datamodel.request.BrowserDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.datamodel.site.SiteDataObject;
import no.sesat.search.datamodel.user.UserDataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.UrlResourceLoader;

import org.apache.log4j.Logger;

/** Ensures that a session is created, and that a new DataModel, with Site and Browser dataObjects,
 * exists within it.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
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

    public void init(final FilterConfig config) throws ServletException {
    }

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
                factory = DataModelFactory.valueOf(new DataModelFactory.Context(){
                    public Site getSite() {
                        return site;
                    }

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

                final ParametersDataObject parametersDO = updateDataModelForRequest(factory, httpRequest);

                datamodel.setParameters(parametersDO);

                if(null == datamodel.getSite() || !datamodel.getSite().getSite().equals(site)){
                    datamodel.setSite(getSiteDO(request, factory));
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

        final DataModel datamodel = factory.instantiate();

        final SiteDataObject siteDO = getSiteDO(request, factory);

        final StringDataObject userAgentDO = factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", request.getHeader("User-Agent")));

        final StringDataObject remoteAddrDO = factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", request.getAttribute("REMOTE_ADDR")));

        final StringDataObject forwardedForDO = factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", request.getHeader("x-forwarded-for")));

        final List<Locale> locales = new ArrayList<Locale>();
        for(Enumeration<Locale> en = request.getLocales(); en.hasMoreElements();){
            locales.add(en.nextElement());
        }

        final BrowserDataObject browserDO = factory.instantiate(
                BrowserDataObject.class,
                new DataObject.Property("userAgent", userAgentDO),
                new DataObject.Property("remoteAddr", remoteAddrDO),
                new DataObject.Property("forwardedFor", forwardedForDO),
                new DataObject.Property("locale", request.getLocale()),
                new DataObject.Property("supportedLocales", locales));

        final UserDataObject userDO = factory.instantiate(
                UserDataObject.class,
                new DataObject.Property("user", null));

        final JunkYardDataObject junkYardDO = factory.instantiate(
                JunkYardDataObject.class,
                new DataObject.Property("values", new ConcurrentHashMap<String,Object>()));

        datamodel.setSite(siteDO);
        datamodel.setBrowser(browserDO);
        datamodel.setUser(userDO);
        datamodel.setJunkYard(junkYardDO);
        //datamodel.setSearches(new ConcurrentHashMap<String,SearchDataObject>());

        return datamodel;
    }

    private static SiteDataObject getSiteDO(final ServletRequest request, final DataModelFactory factory) {

        final Site site = (Site) request.getAttribute(Site.NAME_KEY);
        final SiteConfiguration siteConf = (SiteConfiguration) request.getAttribute(SiteConfiguration.NAME_KEY);

        return factory.instantiate(
                SiteDataObject.class,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConf));
    }

    /** Update the request elements in the datamodel. **/
    private static ParametersDataObject updateDataModelForRequest(
            final DataModelFactory factory,
            final HttpServletRequest request){

        // Note that we do not support String[] parameter values! this is different to pre SESAT days
        final Map<String,StringDataObject> values = new HashMap<String,StringDataObject>();

        for(@SuppressWarnings("unchecked")
                Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ){

            final String key = e.nextElement();
            values.put(key, factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", getParameterSafely(request, key))));
        }

        // Adding all cookies into parameters.
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                values.put(cookie.getName(), factory.instantiate(
                        StringDataObject.class,
                        new DataObject.Property("string", cookie.getValue())));
            }
        }

        final ParametersDataObject parametersDO = factory.instantiate(
                ParametersDataObject.class,
                new DataObject.Property("values", values),
                new DataObject.Property("contextPath", request.getContextPath()),
                new DataObject.Property("uniqueId", SiteLocatorFilter.getRequestId(request)));

        return parametersDO;
    }

    /** Clean out everything in the datamodel that is not flagged to be long-lived. **/
    private static void cleanDataModel(final DataModelFactory factory, final DataModel datamodel){

        for(String key : datamodel.getJunkYard().getValues().keySet()){
            datamodel.getJunkYard().setValue(key, null);
        }

        /* Moving this into SearchServlet so the results can be accessed in later requests */

        //for(String key : datamodel.getSearches().keySet()){
        //    datamodel.setSearch(key, null);
        //}
        //datamodel.setParameters(null);
        //datamodel.setQuery(null);

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

    /** A safer way to get parameters for the query string.
     * Handles ISO-8859-1 and UTF-8 URL encodings.
     *
     * @param request The servlet request we are processing
     * @param parameter The parameter to retrieve
     * @return The correct decoded parameter
     *
     * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
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

                final String encodedReqValue = URLEncoder.encode(value, "UTF-8")
                        .replaceAll("[+]", "%20")
                        .replaceAll("[*]", "%2A");

                queryStringValue = queryStringValue
                        .replaceAll("[+]", "%20")
                        .replaceAll("[*]", "%2A");

                if (!queryStringValue.equalsIgnoreCase(encodedReqValue)){
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
