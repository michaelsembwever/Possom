/* Copyright (2006-2007) Schibsted Søk AS
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
 */
package no.sesat.search.site.config;

import java.io.Serializable;
import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * SiteConfiguration properties.
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
 */
public final class SiteConfiguration implements SiteKeyedFactory,Serializable {

    public static final String NAME_KEY = "SiteConfiguration";
    /**
     *
     */
    public static final String SITE_LOCALE_DEFAULT = "site.locale.default";
    /**
     *
     */
    public static final String PUBLISH_SYSTEM_URL = "publishing.system.baseURL";
    /**
     *
     */
    public static final String PUBLISH_PHYSICAL_HOST = "publishing.system.physicalHost";
    private static final String SITE_LOCALE_SUPPORTED = "site.locale.supported";

    /**
     * Property key to find out if this Site is a sitesearch*
     */
    public static final String IS_SITESEARCH_KEY = "site.issitesearch";

    public static final String DEFAULTTAB_KEY = "site.defaultTab";
    
    public static final String ALLOW_LIST = "site.allow";
    public static final String DISALLOW_LIST = "site.disallow";
    
    public interface Context extends BaseContext, PropertiesContext, SiteContext {}

    private final Properties properties = new Properties();

    private final Site site;
    
    private static final Map<Site, SiteConfiguration> INSTANCES = new HashMap<Site, SiteConfiguration>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    /** No-argument constructor for deserialization. */
    private SiteConfiguration() {
        site = null;
    }
    
    private SiteConfiguration(final Context cxt) {

        try {
            INSTANCES_LOCK.writeLock().lock();
            LOG.trace("SiteConfiguration(cxt)");
            
            site = cxt.getSite();

            cxt.newPropertiesLoader(cxt, Site.CONFIGURATION_FILE, properties).abut();

            INSTANCES.put(cxt.getSite(), this);
            
        }catch(ResourceLoadException rle){
            LOG.fatal("BROKEN SITE HIERARCHY." + rle.getMessage());
            throw new VirtualMachineError(rle.getMessage()){};
            
        } finally {
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    /**
     *
     */
    public Properties getProperties() {

        return properties;
    }

    /**
     *
     */
    public String getProperty(final String key) {

        assert null != key : "Expecting a value for a null key!?";
        final String result = properties.getProperty(key);
        //assert null != result && key.length() > 0 : "Couldn't find " + key + " in " + properties;
        return result;
    }

    /**
     * Find the correct instance handling this Site.
     * We need to use a Context instead of the Site directly so we can handle different styles of loading resources.
     */
    public static SiteConfiguration instanceOf(final Context cxt) {

        final Site site = cxt.getSite();
        assert null != site : "valueOf(cxt) got null site";

        SiteConfiguration instance = null;

        try {
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        } finally {
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            instance = new SiteConfiguration(cxt);
        }
        return instance;
    }

    /**
     * Utility wrapper to the instanceOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load the resource.</b>
     * <b>Therefore can only be used within a running container, eg tomcat.</b>
     */
    public static SiteConfiguration instanceOf(final Site site) {

        // SiteConfiguration.Context for this site & UrlResourceLoader.
        final SiteConfiguration stc = SiteConfiguration.instanceOf(new SiteConfiguration.Context() {
            public Site getSite() {
                return site;
            }

            public boolean doesResourceExist(final String resource) {
                return UrlResourceLoader.doesUrlExist(UrlResourceLoader.getURL(resource, site));
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

    /**
     *
     */
    public boolean remove(final Site site) {

        try {
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        } finally {
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    public boolean isSiteLocaleSupported(final Locale locale) {

        if (Site.DEFAULT.getName().equals(site.getName())) {
            // the DEFAULT site supports all Locales !
            return true;
        }

        final String supportedLocales = getProperty(SITE_LOCALE_SUPPORTED);
        if (null != supportedLocales) {
            final String[] locales = supportedLocales.split(",");
            for (String l : locales) {
                if (locale.toString().equals(l)) {
                    return true;
                }
            }
        }
        return false;
    }
}
