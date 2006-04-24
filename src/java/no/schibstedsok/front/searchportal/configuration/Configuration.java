// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** SearchTabsCreator when SearchModes are serialised in an XML configuration file.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class XMLSearchTabsCreator implements SearchTabsCreator {

    private final Properties properties = new Properties();
    private final XStream xstream = new XStream(new DomDriver());

    private final Context context;

    private final PropertiesLoader propertyLoader;
    private XStreamLoader tabsLoader;

    private SearchTabs tabs;

    private static final Map<Site,XMLSearchTabsCreator> INSTANCES = new HashMap<Site,XMLSearchTabsCreator>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Log LOG = LogFactory.getLog(XMLSearchTabsCreator.class);



    private XMLSearchTabsCreator(final Context cxt) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: XMLSearchTabsCreator()");
        }

        context = cxt;

        propertyLoader = context.newPropertiesLoader(SearchConstants.CONFIGURATION_FILE, properties);

        initialiseXStream();
        INSTANCES_LOCK.writeLock().lock();
        INSTANCES.put(context.getSite(), this);
        INSTANCES_LOCK.writeLock().unlock();
    }

    public SearchTabs getSearchTabs() {

        synchronized (this) {
            if (tabsLoader == null) {
                // unable to put this in the constructor because it relies on properties being loaded.
                tabsLoader = context.newXStreamLoader(getProperties().getProperty("tabs_configuration"), xstream);
            }
        }

        tabsLoader.abut();

        tabs = (SearchTabs) tabsLoader.getXStreamResult();

        LOG.info("Tabs created from "
                + properties.getProperty("tabs_configuration"));

        return tabs;
    }

    private void initialiseXStream() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: createSearchTabsImpl()");
        }

        xstream.alias(
                        "FastSearch",
                        FastConfiguration.class);
        xstream.alias(
                        "YellowSearch",
                        YellowSearchConfiguration.class);
        xstream.alias(
                        "WhiteSearch",
                        WhiteSearchConfiguration.class);
        xstream.alias(
                        "WebSearch",
                        WebSearchConfiguration.class);
        xstream.alias(
                        "NewsSearch",
                        NewsSearchConfiguration.class);
        xstream.alias(
                        "PicSearch",
                        PicSearchConfiguration.class);
        xstream.alias(
                        "tabs",
                        SearchTabs.class);
        xstream.alias(
                        "OverturePPCSearch",
                        OverturePPCConfiguration.class);
        xstream.alias(
                        "MathExpression",
                        MathExpressionConfiguration.class);
        xstream.alias(
                        "StockSearch",
                        StockSearchConfiguration.class);

    }

    public Properties getProperties() {
        propertyLoader.abut();
        return properties;
    }

    /** Find the correct instance handling this Site.
     * We need to use a Context instead of the Site directly so we can handle different styles of loading resources.
     **/
    public static SearchTabsCreator valueOf(final Context cxt) {
        final Site site = cxt.getSite();

        INSTANCES_LOCK.readLock().lock();
        SearchTabsCreator instance = INSTANCES.get(site);
        INSTANCES_LOCK.readLock().unlock();

        if (instance == null) {
            instance = new XMLSearchTabsCreator(cxt);
        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load all resources.</b>
     */
    public static SearchTabsCreator valueOf(final Site site) {

        // XMLSearchTabsCreator.Context for this site & UrlResourceLoader.
        final SearchTabsCreator stc = XMLSearchTabsCreator.valueOf(new SearchTabsCreator.Context() {
            public Site getSite() {
                return site;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return UrlResourceLoader.newXStreamLoader(this, resource, xstream);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }

        });
        return stc;
    }

    public static boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }
}
