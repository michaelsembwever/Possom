/*
 * Copyright (2007) Schibsted Søk AS
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
 */

package no.sesat.search.run.handler;

import java.util.Locale;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.site.SiteDataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.SiteConfiguration;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import no.sesat.search.site.Site.Context;

/**
 * $Id$
 * @author <a href="mailto:anders@jamtli.no">Anders Johan Jamtli</a>
 */
public class RunHandlerFactoryTest {
    @Test
    final public void testGetController() throws Exception {
        final DataModelFactory factory = DataModelFactory.instanceOf(new DataModelFactory.Context() {
            public Site getSite() {
                return Site.DEFAULT;
            }
            
            public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {
                return TestResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
        });
        
        final DataModel datamodel = factory.instantiate();
        
        final Site site = getTestingSite();
        
        final SiteConfiguration.Context cxt = new SiteConfiguration.Context() {
            public Site getSite() {
                return site;
            }
            
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteContext,
                    final String resource,
                    final Properties properties) {
                return TestResourceLoader.newPropertiesLoader(siteContext, resource, properties);
            }
        };
        
        final SiteConfiguration siteConfig = SiteConfiguration.instanceOf(cxt);
        
        final SiteDataObject siteDO = factory.instantiate(
                SiteDataObject.class,
                datamodel,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConfig));
        datamodel.setSite(siteDO);

        final RunHandler.Context context = new RunHandler.Context() {

            public DataModel getDataModel() {
                return datamodel;
            }

            public PropertiesLoader newPropertiesLoader(SiteContext siteCxt, String resource, Properties properties) {
                return TestResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }

            public Site getSite() {
                return datamodel.getSite().getSite();
            }

            public DocumentLoader newDocumentLoader(SiteContext siteCxt, String resource, DocumentBuilder builder) {
                return TestResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }

            public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jarFileName) {
                return TestResourceLoader.newBytecodeLoader(siteContext, className, className);
            }
        };

        final RunHandlerConfig config = new TestRunHandlerConfig();

        final RunHandler rh = RunHandlerFactory.getController(context, config);
        assert rh != null;
    }

    protected Site getTestingSite(){
        
        final String basedir = "localhost";
        return Site.valueOf(
                getSiteConstructingContext(),
                basedir,
                Locale.getDefault());
    }    

    protected Site.Context getSiteConstructingContext(){
        
        return new Context(){
            public String getParentSiteName(final SiteContext siteContext){
                // we have to do this manually instead of using SiteConfiguration,
                //  because SiteConfiguration relies on the parent site that we haven't get initialised.
                // That is, the PARENT_SITE_KEY property MUST be explicit in the site's configuration.properties.
                final Properties props = new Properties();
                final PropertiesLoader loader
                        = TestResourceLoader.newPropertiesLoader(siteContext, Site.CONFIGURATION_FILE, props);
                loader.abut();
                return props.getProperty(Site.PARENT_SITE_KEY);
            }
        };
    }
}

class TestResourceLoader extends FileResourceLoader {
    
    private static final Logger LOG = Logger.getLogger(TestResourceLoader.class);
    
    protected TestResourceLoader(final SiteContext cxt) {
        super(cxt);
    }
    
    public static PropertiesLoader newPropertiesLoader(
            final SiteContext siteCxt,
            final String resource,
            final Properties properties) {
    
        properties.putAll(props);
        final PropertiesLoader pl = new TestResourceLoader(siteCxt);
        return pl;
    }
    
    @Override
    public void init(final String resource, final Properties properties) {}
    
    private static final Properties props = new Properties();
    static {
        props.setProperty(Site.DEFAULT_SITE_LOCALE_KEY, "no");
        props.setProperty("sesam.datamodel.impl", "no.sesat.search.datamodel.DataModelFactoryImpl");
        props.setProperty(Site.DEFAULT_SERVER_PORT_KEY, "0");
        props.setProperty(Site.DEFAULT_SITE_KEY, "localhost");
        props.setProperty(Site.PARENT_SITE_KEY, "");
    }
    
    @Override
    public Properties getProperties() {
        return props;
    }
    
    @Override
    public void abut() {}
}
