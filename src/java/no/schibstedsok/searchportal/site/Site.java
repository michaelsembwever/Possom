/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Site.java
 *
 * Created on 22 January 2006, 13:48
 *
 */

package no.schibstedsok.searchportal.site;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import no.schibstedsok.common.ioc.BaseContext;
import org.apache.log4j.Logger;

/** A Site object idenetifies an unique SiteSearch implementation.
 * This bean holds nothing more than the name of the virtual host used to access this SiteSearch.
 * <b>Immutable</b>.
 *
 * Does a little bit of niggling wiggling to load the DEFAULT site.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class Site {

    /** Not to be confused with the SiteContext.
     * This is a Context required for constructing a Site.
     * While a SiteContext is a context required to use a Site.
     **/
    public interface Context extends BaseContext{
        /** TODO comment me. **/
        String getParentSiteName(SiteContext siteContext);
    }

    private static final Logger LOG = Logger.getLogger(Site.class);

    private static final String FATAL_CANT_FIND_DEFAULT_SITE
            = "Could not load the property \"site.default\" from configuration.properties"
            + "to define what the default site is.";

    /** Found from the configuration.properties resource found in this class's ClassLoader. **/
    private static final String DEFAULT_SITE_KEY = "site.default";
    /** TODO comment me. **/
    public static final String PARENT_SITE_KEY = "site.parent";
    private static final String DEFAULT_SITE_LOCALE_KEY = "site.default.locale.default";
    /** TODO comment me. **/
    public static final String NAME_KEY = "site";
    /** TODO comment me. **/
    public static final String CONFIGURATION_FILE = "configuration.properties";

    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map<String,Site> INSTANCES = new HashMap<String,Site>();

     /**
     * Holds value of property siteName.
     */
    private final String siteName;
    /**
     * Holds value of property cxtName.
     */
    private final String cxtName;
    /**
     * Holds value of property _locale.
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

    /** Creates a new instance of Site. 
     * A null Context will result in a parentSiteName == siteName
     */
    private Site(final Context cxt, final String theSiteName, final Locale theLlocale) {
        // siteName must finish with a '\'
        siteName = ensureTrailingSlash(theSiteName);

        cxtName = siteName.indexOf(':') >= 0
            ? siteName.substring(0, siteName.indexOf(':')) + '/' // don't include the port in the cxtName.
            : siteName;
        locale = theLlocale;
        uniqueName = getUniqueName(siteName, locale);
        // register in global pool.
        INSTANCES.put(uniqueName, this);

        final Site thisSite = this;

        final SiteContext siteContext = new SiteContext() {
            public Site getSite() {
                return thisSite;
            }
        };

        
        final String parentSiteName = null != cxt ? cxt.getParentSiteName(siteContext) : siteName;

        parent = null == parentSiteName || ensureTrailingSlash(parentSiteName).equals(siteName)
            ? null
            : Site.valueOf(cxt, parentSiteName, theLlocale);
    }


    /** the parent to this site.
     * returns null if we are the DEFAULT site.
     **/
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

    /** {@inheritDoc}
     */
    public String toString(){
        return uniqueName;
    }

    /** {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        return obj instanceof Site
                ? uniqueName.equals(((Site)obj).uniqueName)
                : super.equals(obj);
    }

    /** {@inheritDoc}
     */
    public int hashCode() {
        return uniqueName.hashCode();
    }

    /** Get the instance for the given siteName.
     * A "www." prefix will be automatically ignored.
     * @param cxt the cxt to use during creation. null will prevent constructing a new site.
     * @param siteName the virtual host name.
     * @return the site bean.
     */
    public static Site valueOf(final Context cxt, final String siteName, final Locale locale) {

        // Strip www. from siteName
        final String shortSiteName =
                ensureTrailingSlash(siteName.replaceAll("www.", ""));

        Site site = INSTANCES.get(getUniqueName(shortSiteName,locale));
        if (null == site && null != cxt) {
            site = new Site(cxt, shortSiteName, locale);
        }
        return site;
    }

    // should never used except in catastrophe.
    private static final String SITE_DEFAULT_FALLBACK = "sesam.no";
    private static final String SITE_DEFAULT_LOCALE_FALLBACK = "no";

    static {

        final Properties props = new Properties();
        try  {
            props.load(Site.class.getResourceAsStream('/' + CONFIGURATION_FILE));
        }  catch (IOException ex) {
            LOG.fatal(FATAL_CANT_FIND_DEFAULT_SITE, ex);
        }
        final String defaultSiteName = props.getProperty(DEFAULT_SITE_KEY, SITE_DEFAULT_FALLBACK);
        final String defaultSiteLocaleName = props.getProperty(DEFAULT_SITE_LOCALE_KEY, SITE_DEFAULT_LOCALE_FALLBACK);

        DEFAULT = new Site(null, defaultSiteName, new Locale(defaultSiteLocaleName));
    }

    /** the default SiteSearch. For example: "sesam.no" or "localhost:8080".
     */
    public static final Site DEFAULT;

    /** TODO comment me. **/
    public static String getUniqueName(final String siteName, final Locale locale) {
        return siteName+"["+locale.getDisplayName()+"]";
    }

    private static String ensureTrailingSlash(final String theSiteName) {
        return theSiteName.endsWith("/")
            ? theSiteName
            : theSiteName + '/';
    }

    

}
