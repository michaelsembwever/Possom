// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.mode.config.*;
import no.schibstedsok.searchportal.util.config.PropertiesContext;

import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.util.config.UrlResourceLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import org.apache.log4j.Logger;


/**
 * SiteConfiguration properties.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 2720 $</tt>
 */
public final class SiteConfiguration implements SiteKeyedFactory{

    public static final String CONFIGURATION_FILE = "configuration.properties";
    public static final String SITE_LOCALE_DEFAULT = "site.locale.default";
    public static final String PUBLISH_SYSTEM_URL = "publishing.system.baseURL";
    public static final String PUBLISH_SYSTEM_HOST = "publishing.system.host-header";

    public interface Context extends BaseContext, PropertiesContext, SiteContext {
    }

    private final Properties properties = new Properties();

    private final Context context;

    private final PropertiesLoader propertyLoader;

    private static final Map<Site, SiteConfiguration> INSTANCES = new HashMap<Site,SiteConfiguration>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    private SiteConfiguration(final Context cxt) {

        try{
            INSTANCES_LOCK.writeLock().lock();
            LOG.trace("Configuration(cxt)");
            context = cxt;

            propertyLoader = context.newPropertiesLoader(CONFIGURATION_FILE, properties);

            INSTANCES.put(context.getSite(), this);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    public Properties getProperties() {

        if(properties.size() == 0){
            propertyLoader.abut();
        }
        return properties;
    }

    public String getProperty(final String key) {

        if(properties.size() == 0){
            propertyLoader.abut();
        }
        return properties.getProperty(key);
    }

    /** Find the correct instance handling this Site.
     * We need to use a Context instead of the Site directly so we can handle different styles of loading resources.
     **/
    public static SiteConfiguration valueOf(final Context cxt) {
        final Site site = cxt.getSite();

        INSTANCES_LOCK.readLock().lock();
        SiteConfiguration instance = INSTANCES.get(site);
        INSTANCES_LOCK.readLock().unlock();

        if (instance == null) {
            instance = new SiteConfiguration(cxt);
        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load the resource.</b>
     */
    public static SiteConfiguration valueOf(final Site site) {

        // SiteConfiguration.Context for this site & UrlResourceLoader.
        final SiteConfiguration stc = SiteConfiguration.valueOf(new SiteConfiguration.Context() {
            public Site getSite() {
                return site;
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
        });
        return stc;
    }

    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }
}
