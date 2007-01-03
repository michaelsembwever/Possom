// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.site.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import org.apache.log4j.Logger;


/**
 * SiteConfiguration properties.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version $Id$
 */
public final class SiteConfiguration implements SiteKeyedFactory{


    /** TODO comment me. **/
    public static final String SITE_LOCALE_DEFAULT = "site.locale.default";
    /** TODO comment me. **/
    public static final String PUBLISH_SYSTEM_URL = "publishing.system.baseURL";
    /** TODO comment me. **/
    public static final String PUBLISH_SYSTEM_HOST = "publishing.system.host-header";
    private static final String SITE_LOCALE_SUPPORTED = "site.locale.supported";

    public interface Context extends BaseContext, PropertiesContext, SiteContext {
    }

    private final Properties properties = new Properties();

    private final Context context;

    private static final Map<Site, SiteConfiguration> INSTANCES = new HashMap<Site,SiteConfiguration>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    private SiteConfiguration(final Context cxt) {

        try{
            INSTANCES_LOCK.writeLock().lock();
            LOG.trace("SiteConfiguration(cxt)");
            context = cxt;

            context.newPropertiesLoader(cxt, Site.CONFIGURATION_FILE, properties).abut();

            INSTANCES.put(context.getSite(), this);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    /** TODO comment me. **/
    public Properties getProperties() {
        
        return properties;
    }

    /** TODO comment me. **/
    public String getProperty(final String key) {
        
        assert null != key : "Expecting a value for a null key!?";
        final String result = properties.getProperty(key);
        //assert null != result && key.length() > 0 : "Couldn't find " + key + " in " + properties;
        return result;
    }

    /** Find the correct instance handling this Site.
     * We need to use a Context instead of the Site directly so we can handle different styles of loading resources.
     **/
    public static SiteConfiguration valueOf(final Context cxt) {
        
        final Site site = cxt.getSite();
        assert null != site : "valueOf(cxt) got null site";
        
        SiteConfiguration instance = null;
        
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }
        
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
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final Properties properties) {
                
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
        });
        return stc;
    }

    /** TODO comment me. **/
    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }
    
    public boolean isSiteLocaleSupported(final Locale locale){
        
        if( Site.DEFAULT.getName().equals(context.getSite().getName())){
            // the DEFAULT site supports all Locales !
            return true;
        }
        final String supportedLocales = getProperty(SITE_LOCALE_SUPPORTED);
        if( null != supportedLocales ){
            final String[] locales = supportedLocales.split(",");
            for(String l : locales){
                if(locale.toString().equals(l)){
                    return true;
                }
            }
        }
        return false;
    }
}
