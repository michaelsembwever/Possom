/* Copyright (2005-2008) Schibsted Søk AS
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
 *
 * DataModelTestCase.java
 *
 * Created on June 28, 2006, 12:06 PM
 *
 */

package no.sesat.search.datamodel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.junkyard.JunkYardDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.datamodel.site.SiteDataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.SiteConfiguration;

/**
 * @version $Id$
 *
 */
public abstract class DataModelTestCase extends SiteTestCase{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelTestCase.class);

    // Attributes ----------------------------------------------------

    private DataModelFactory factory = null;
    private Site site = null;
    private SiteConfiguration.Context siteConfCxt = null;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    protected DataModelTestCase(){}

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // no.sesat.search.TestCase overrides ----------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected synchronized void setSiteConfigurationContext(final SiteConfiguration.Context siteConfCxt) {
        this.siteConfCxt = siteConfCxt;
    }

    protected synchronized DataModelFactory getDataModelFactory() throws SiteKeyedFactoryInstantiationException{

        if(null == factory){
            try{
                site = getTestingSite();
                if (siteConfCxt == null) {
                    siteConfCxt = new SiteConfiguration.Context(){
                        public Site getSite() {
                            return site;
                        }

                        public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                                    final String resource,
                                                                    final Properties properties) {
                            return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                        }
                    };
                }
                factory = DataModelFactory.instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, siteConfCxt));

            }catch(SiteKeyedFactoryInstantiationException skfie){
                LOG.error(skfie.getMessage(), skfie);
                throw skfie;
            }
        }
        return factory;
    }

    /** This returns a NEW datamodel every call!!
     * @return
     * @throws SiteKeyedFactoryInstantiationException when unable to instantiate SiteConfiguration
     */
    protected DataModel getDataModel() throws SiteKeyedFactoryInstantiationException{

        getDataModelFactory();

        final DataModel datamodel = factory.instantiate();

        final SiteConfiguration siteConfig = SiteConfiguration.instanceOf(siteConfCxt);

        final SiteDataObject siteDO = factory.instantiate(
                SiteDataObject.class,
                datamodel,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConfig));

        final ParametersDataObject parametersDO = factory.instantiate(
                ParametersDataObject.class,
                datamodel,
                new DataObject.Property("values", new HashMap<String,StringDataObject>()),
                new DataObject.Property("contextPath", "/"),
                new DataObject.Property("uniqueId", UUID.randomUUID().toString()));

        final JunkYardDataObject junkYardDO = factory.instantiate(
                JunkYardDataObject.class,
                datamodel,
                new DataObject.Property("values", new Hashtable<String,Object>()));

        datamodel.setSite(siteDO);
        datamodel.setParameters(parametersDO);
        datamodel.setJunkYard(junkYardDO);

        return datamodel;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------





}
