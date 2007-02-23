/* Copyright (2005-2006) Schibsted Søk AS
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

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    
    protected DataModelTestCase(){}

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // no.schibstedsok.searchportal.TestCase overrides ----------------------------  

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    
    protected DataModel getDataModel() throws SiteKeyedFactoryInstantiationException{
        
        final DataModelFactory factory;
        final Site site = super.getTestingSite();
        final SiteConfiguration.Context cxt = new SiteConfiguration.Context(){
                public Site getSite() {
                    return site;
                }

                public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                            final String resource,
                                                            final Properties properties) {
                    return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                }
            };
        final SiteConfiguration siteConf = SiteConfiguration.valueOf(cxt);
        
        try{
            factory = DataModelFactory.valueOf(ContextWrapper.wrap(DataModelFactory.Context.class, cxt));

        }catch(SiteKeyedFactoryInstantiationException skfie){
            LOG.error(skfie.getMessage(), skfie);
            throw skfie;
        }
        
        final DataModel datamodel = factory.instantiate();

        final SiteDataObject siteDO = factory.instantiate(
                SiteDataObject.class,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConf));
        
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
