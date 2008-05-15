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
 * Site.java
 *
 * Created on 22 January 2006, 13:48
 *
 */

package no.sesat.search.site;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.BaseContext;
import org.apache.log4j.Level;
import no.sesat.search.site.config.ResourceLoadException;
import org.apache.log4j.Logger;

/** A Site object identifies a Skin + Locale pairing.
 *  
 * This bean holds nothing more than the name of the virtual host (siteName) and locale used to access this Skin.
 * 
 * It is used as a key to obtain the correct factory instances in the application.
 * There is usually only one Skin per siteName and it is left up to the skin to handle different locales.
 * .
 * <b>Immutable</b>.
 *
 * Does a little bit of niggling wiggling to load the DEFAULT site. See static constructor.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class Site implements Serializable {

    /** During construction of any site we must know who the parent site is going to be. 
     * It will likely be also constructed since leaf sites are those first requested.
     * 
     * The base level sites, locale variants of the DEFAULT site, are expected to have a null parent.
     * These DEFAULT sites can just pass in a null context instead of constructing a context that returns a null parent.
     * 
     * Not to be confused with the SiteContext.
     * This is a Context required for constructing a Site.
     * While a SiteContext is a context required to use a Site.
     **/
    public interface Context extends BaseContext{
        /** Get the name of the parent site.
         * @param siteContext that can provide the current site the code is within.
         * @return the name of the parent site.
         */
        String getParentSiteName(SiteContext siteContext);
    }

    private static final Logger LOG = Logger.getLogger(Site.class);

    private static final String FATAL_CANT_FIND_DEFAULT_SITE
            = "Could not load the property \"site.default\" from configuration.properties"
            + "to define what the default site is.";

    /** Found from the configuration.properties resource found in this class's ClassLoader. **/
    public static final String DEFAULT_SITE_KEY = "site.default";
    public static final String DEFAULT_SITE_LOCALE_KEY = "site.default.locale.default";
    public static final String DEFAULT_SERVER_PORT_KEY = "server.port";
    /** Property key for site parent's name. **/
    public static final String PARENT_SITE_KEY = "site.parent";
    /** Property key for a site object. **/
    public static final String NAME_KEY = "site";
    /** Name of the resource to find the PARENT_SITE_KEY property. **/
    public static final String CONFIGURATION_FILE = "configuration.properties";
    private static final String CORE_CONF_FILE = "core.properties";
    private static final Map<String,Site> INSTANCES = new HashMap<String,Site>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static volatile boolean constructingDefault = false;

     /**
     * Holds value of property siteName.
     */
    private final String siteName;
    /**
     * Holds value of property cxtName.
     */
    private final String cxtName;
    /**
     * Holds value of property locale.
     */
    private Locale locale;
    /**
     * Holds value of property uniqueName.
     */
    private final String uniqueName;

   /**
    * Holds value of property parent.
    */
    private final Site parent;

    /** No-argument constructor for deserialization. */
    private Site() {
        siteName = null;
        cxtName = null;
        locale = Locale.getDefault();
        uniqueName = null;
        parent = null;
    }

    /** Creates a new instance of Site.
     * A null Context will result in a parentSiteName == siteName
     * @throws IllegalArgumentException when there exists no skin matching the theSiteName argument.
     */
    private Site(final Context cxt, final String theSiteName, final Locale theLocale) {


        try{
            INSTANCES_LOCK.writeLock().lock();

            LOG.info("Site(cxt, " + theSiteName + ", " + theLocale + ')');
            assert null != theSiteName;
            assert null != theLocale;

            // siteName must finish with a '\'
            siteName = ensureTrailingSlash(theSiteName);

            cxtName = ensureTrailingSlash(siteName.replaceAll(":.*$", "")); // don't include the port in the cxtName.
            locale = theLocale;
            uniqueName = getUniqueName(siteName, locale);

            final Site thisSite = this;

            final SiteContext siteContext = new SiteContext() {
                public Site getSite() {
                    return thisSite;
                }
            };

            final String parentSiteName;
            if(null != cxt){
                // cxt.getParentSiteName(siteContext) is an expensive call due to resource load every call.
                final String psn = cxt.getParentSiteName(siteContext);
                parentSiteName = null != psn ? ensureTrailingSlash(psn) : null;
                
            }else{
                parentSiteName = siteName;
            }

            final String tsParentNameNoPort = null != parentSiteName
                    ? ensureTrailingSlash(parentSiteName.replaceAll(":.*$", ""))
                    : null;

            LOG.debug(siteName + " parent is " + parentSiteName);

            if(constructingDefault || DEFAULT.getName().equals(cxtName)){
                // dont let the original DEFAULT have a parent
                // OR let a port-explicit generic.sesam have a parent.
                parent = null;

            }else{
                LOG.debug("Default-check-> " + DEFAULT.getName() + " ?= " + parentSiteName);

                final boolean invalidParent = null == parentSiteName
                        // also avoid any incest
                        || siteName.equals(tsParentNameNoPort)
                        // and detect ahead the link to the grandfather of all "generic.sesam"
                        || DEFAULT.getName().equals(parentSiteName);

                parent = invalidParent
                    ? DEFAULT
                    : Site.valueOf(cxt, parentSiteName, theLocale);
            }

            assert null != parent || constructingDefault || DEFAULT.getName().equals(cxtName)
                    : "Parent must exist for all Sites except the DEFAULT";

            // register in global pool.
            LOG.debug("INSTANCES.put(" + uniqueName + ", this)");
            INSTANCES.put(uniqueName, this);

        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }


    /** the parent to this site.
     * @return site null if we are the DEFAULT site.
     */
    public Site getParent(){
        return parent;
    }

    /**
     * Getter for property siteName.
     * Guaranteed to have "www." prefix stripped.
     * Guaranteed to finish with '/'.
     * @return Value of property siteName.
     */
    public String getName() {
        return siteName;
    }

    /**
     * Getter for property cxtName.
     * Same as name but without port specification.
     * Guaranteed to finish with '/'.
     * @return Value of property cxtName.
     */
    public String getConfigContext() {
        return cxtName;
    }

    /**
     * Getter for property (velocity) template directory.
     * Absolute URL to directory (velocity) template is found for this site.
     * <b>Does not</b> finish with '/'. Reads nicer in template statements.
     * @return Value of property (velocity) template directory.
     */
    public String getTemplateDir() {
        return "http://" + siteName + cxtName + "templates";
    }


    /**
     * Getter for property locale.
     * @return Value of property locale.
     */
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString(){
        return uniqueName;
    }

    @Override
    public boolean equals(final Object obj) {

        return obj instanceof Site
                ? uniqueName.equals(((Site)obj).uniqueName)
                : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return uniqueName.hashCode();
    }

    /** Get the instance for the given siteName.
     * The port number will be changed if the server has explicitly assigned one port number to use.
     * A "www." prefix will be automatically ignored.
     * 
     * TODO refactor to instanceOf(..). it is an instance returned not a primitive.
     * 
     * @param cxt the cxt to use during creation. null will prevent constructing a new site.
     * @param siteName the virtual host name.
     * @param locale the locale desired
     * @throws IllegalArgumentException when there exists no skin matching the siteName argument.
     * @return the site instance. never null.
     */
    public static Site valueOf(final Context cxt, final String siteName, final Locale locale) {

        Site site = null;

        // Strip www. from siteName
        final String realSiteName = ensureTrailingSlash(siteName.replaceAll("www.", ""));

        // Look for existing instances
        final String uniqueName = getUniqueName(realSiteName,locale);
        try{
            INSTANCES_LOCK.readLock().lock();
            LOG.trace("INSTANCES.get(" + uniqueName + ")");
            site = INSTANCES.get(uniqueName);

        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        // construct a new instance
        if (null == site && null != cxt) {
            site = new Site(cxt, realSiteName, locale);
        }
        return site;
    }

    static {

        final Properties props = new Properties();
        final InputStream is = Site.class.getResourceAsStream('/' + CORE_CONF_FILE);

        try {
            if(null != is){
                props.load(is);
                is.close();
            }

        }  catch (IOException ex) {
            LOG.fatal(FATAL_CANT_FIND_DEFAULT_SITE, ex);
        }

        final Level oLevel = LOG.getLevel();
        LOG.setLevel(Level.ALL);
        
        final String defaultSiteName = props.getProperty(DEFAULT_SITE_KEY, System.getProperty(DEFAULT_SITE_KEY));
        LOG.info("defaultSiteName: " + defaultSiteName);

        final String defaultSiteLocaleName
                = props.getProperty(DEFAULT_SITE_LOCALE_KEY, System.getProperty(DEFAULT_SITE_LOCALE_KEY));
        LOG.info("defaultSiteLocaleName: " + defaultSiteLocaleName);

        final String defaultSitePort
                = props.getProperty(DEFAULT_SERVER_PORT_KEY, System.getProperty(DEFAULT_SERVER_PORT_KEY));
        LOG.info("defaultSitePort: " + defaultSitePort);
        
        LOG.setLevel(oLevel);
        
        SERVER_PORT = Integer.parseInt(defaultSitePort);

        constructingDefault = true;
        final Locale defaultLocale = new Locale(defaultSiteLocaleName);
        DEFAULT = new Site(null, defaultSiteName, defaultLocale);
        // All locales along-side DEFAULT
        for(Locale l : Locale.getAvailableLocales()){
            if(defaultLocale != l){
                new Site(null, defaultSiteName, l);
            }
        }
        constructingDefault = false;
    }

    /** the default SiteSearch. For example: "generic.sesam.no" or "generic.localhost:8080".
     */
    public static final Site DEFAULT;

    /** the server's actual port. **/
    public static final int SERVER_PORT;

    /** Get a uniqueName given a pair of siteName and locale.
     * Used internally and before construction of any new Site.
     *
     * @param siteName
     * @param locale
     * @return
     */
    public static String getUniqueName(final String siteName, final Locale locale) {

        return siteName + '[' + locale.getDisplayName() + ']';
    }

    private static String ensureTrailingSlash(final String theSiteName) {

        return theSiteName.endsWith("/")
            ? theSiteName
            : theSiteName + '/';
    }
}
