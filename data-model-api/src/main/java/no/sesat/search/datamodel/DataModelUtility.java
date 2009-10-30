/* Copyright (2009) Schibsted ASA
 *   This file is part of SESAT.
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
 *
 * ImportPublish.java
 *
 * Created on 12 March 2007, 15:38
 *
 */

package no.sesat.search.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.junkyard.JunkYardDataObject;
import no.sesat.search.datamodel.request.BrowserDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.datamodel.site.SiteDataObject;
import no.sesat.search.datamodel.user.UserDataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteConfiguration;
import org.apache.log4j.Logger;


/** Provides utility methods around the DataModel class.
 *
 * @version <tt>$Id$</tt>
 */
public final class DataModelUtility {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelUtility.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    public static DataModel createDataModel(
            final DataModelFactory factory,
            final Site site,
            final SiteConfiguration siteConf,
            final String userAgent,
            final String remoteAddr,
            final String xForwardedFor,
            final Locale locale,
            final List<Locale> locales){

        final DataModel datamodel = factory.instantiate();

        final SiteDataObject siteDO = getSiteDO(factory, datamodel, site, siteConf);

        final StringDataObject userAgentDO = factory.instantiate(
                StringDataObject.class,
                datamodel,
                new DataObject.Property("string", userAgent));

        final StringDataObject remoteAddrDO = factory.instantiate(
                StringDataObject.class,
                datamodel,
                new DataObject.Property("string", remoteAddr));

        final StringDataObject forwardedForDO = factory.instantiate(
                StringDataObject.class,
                datamodel,
                new DataObject.Property("string", xForwardedFor));

        final BrowserDataObject browserDO = factory.instantiate(
                BrowserDataObject.class,
                datamodel,
                new DataObject.Property("userAgent", userAgentDO),
                new DataObject.Property("remoteAddr", remoteAddrDO),
                new DataObject.Property("forwardedFor", forwardedForDO),
                new DataObject.Property("locale", locale),
                new DataObject.Property("supportedLocales", locales));

        final UserDataObject userDO = factory.instantiate(
                UserDataObject.class,
                datamodel,
                new DataObject.Property("user", null));

        final JunkYardDataObject junkYardDO = factory.instantiate(
                JunkYardDataObject.class,
                datamodel,
                new DataObject.Property("values", new ConcurrentHashMap<String,Object>(5, 0.75f, 2)));

        datamodel.setSite(siteDO);
        datamodel.setBrowser(browserDO);
        datamodel.setUser(userDO);
        datamodel.setJunkYard(junkYardDO);

        return datamodel;
    }

    public static SiteDataObject getSiteDO(
            final DataModelFactory factory,
            final DataModel datamodel,
            final Site site,
            final SiteConfiguration siteConf) {

        return factory.instantiate(
                SiteDataObject.class,
                datamodel,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConf));
    }

    public static ParametersDataObject updateDataModelForRequest(
            final DataModelFactory factory,
            final DataModel datamodel,
            final String contextPath,
            final Map<String,Map<String,String>> parameterMapsByType,
            final String uniqueRequestId){

        final Map<String,StringDataObject> values = new HashMap<String,StringDataObject>();

        for(String type : parameterMapsByType.keySet()){
            for(Map.Entry<String,String> entry : parameterMapsByType.get(type).entrySet()){

                values.put(entry.getKey(), factory.instantiate(
                    StringDataObject.class,
                    datamodel,
                    new DataObject.Property("string", entry.getValue())));

                // meta-data noting this is a parameter of "type"
                values.put(entry.getKey() + "-is" + type, factory.instantiate(
                        StringDataObject.class,
                        datamodel,
                        new DataObject.Property("string", "true")));
            }
        }

        final ParametersDataObject parametersDO = factory.instantiate(
                ParametersDataObject.class,
                datamodel,
                new DataObject.Property("values", values),
                new DataObject.Property("contextPath", contextPath),
                new DataObject.Property("uniqueId", uniqueRequestId));

        return parametersDO;
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of NewClass */
    private DataModelUtility() {
    }

    // Public --------------------------------------------------------


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
