/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * DataModelTestCase.java
 *
 * Created on June 28, 2006, 12:06 PM
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.util.Hashtable;
import java.util.Properties;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.junkyard.JunkYardDataObject;
import no.schibstedsok.searchportal.datamodel.site.SiteDataObject;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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

    // no.schibstedsok.searchportal.TestCase overrides ----------------------------  

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    
    protected synchronized DataModelFactory getDataModelFactory() throws SiteKeyedFactoryInstantiationException{
        
        if(null == factory){
            try{
                site = getTestingSite();
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
                factory = DataModelFactory.valueOf(ContextWrapper.wrap(DataModelFactory.Context.class, siteConfCxt));

            }catch(SiteKeyedFactoryInstantiationException skfie){
                LOG.error(skfie.getMessage(), skfie);
                throw skfie;
            }
        }
        return factory;
    }
    
    protected DataModel getDataModel() throws SiteKeyedFactoryInstantiationException{

        getDataModelFactory();
       
        final DataModel datamodel = factory.instantiate();

        final SiteConfiguration siteConfig = SiteConfiguration.valueOf(siteConfCxt);
        
        final SiteDataObject siteDO = factory.instantiate(
                SiteDataObject.class,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConfig));
        
        final JunkYardDataObject junkYardDO = factory.instantiate(
                JunkYardDataObject.class,
                new DataObject.Property("values", new Hashtable<String,Object>()));

        datamodel.setSite(siteDO);
        datamodel.setJunkYard(junkYardDO);
        
        return datamodel;
    }

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------



    
    
}
