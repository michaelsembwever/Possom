/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Site.java
 *
 * Created on 22 January 2006, 13:48
 *
 */

package no.schibstedsok.front.searchportal.site;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static final Log LOG = LogFactory.getLog(Site.class);

    private static final String FATAL_CANT_FIND_DEFAULT_SITE
            = "Could not load the property \"site.default\" from configuration.properties"
            + "to define what the default site is.";

    /** Found from the configuration.properties resource found in this class's ClassLoader. **/
    private static final String DEFAULT_SITE_KEY = "site.default";

    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map/*<String,Site>*/ INSTANCES = new HashMap/*<String,Site>*/();

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
    

    /** Creates a new instance of Site. */
    private Site(final String siteName, final Locale locale) {
        
        // siteName must finish with a '\'
        this.siteName = siteName.endsWith("/")
            ? siteName
            : siteName + '/';
        this.cxtName = siteName.indexOf(':') >= 0
            ? siteName.substring(0, siteName.indexOf(':')) + "/" // don't include the port in the cxtName.
            : siteName;
        this.locale = locale;
        // register in global pool.
        INSTANCES.put(getUniqueName(siteName,locale), this);
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
     * Getter for property css directory.
     * Absolute URL to directory css is found for this site.
     * <b>Does not</b> finish with '/'. Reads nicer in template statements.
     * @return Value of property css directory.
     */
    public String getCssDir() {
        return "http://" + siteName + cxtName + "css";
    }

    /**
     * Getter for property javascript directory.
     * Absolute URL to directory javascript is found for this site.
     * <b>Does not</b> finish with '/'. Reads nicer in template statements.
     * @return Value of property javascript directory.
     */
    public String getJsDir() {
        return "http://" + siteName + cxtName + "javascript";
    }

    /**
     * Getter for property image directory.
     * Absolute URL to directory image is found for this site.
     * <b>Does not</b> finish with '/'. Reads nicer in template statements.
     * @return Value of property image directory.
     */
    public String getImageDir() {
        return "http://" + siteName + cxtName + "images";
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
        return getUniqueName(siteName,locale);
    }
    
    /** {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return obj instanceof Site
                ? getUniqueName(siteName,locale).equals(getUniqueName(((Site)obj).siteName,((Site)obj).locale))
                : super.equals(obj);
    }

    /** {@inheritDoc}
     */
    public int hashCode() {
        return getUniqueName(siteName,locale).hashCode();
    }    
    
    /** Get the instance for the given siteName.
     * A "www." prefix will be automatically ignored.
     * @param siteName the virtual host name.
     * @return the site bean.
     */
    public static Site valueOf(final String siteName, final Locale locale) {

        final String shortSiteName = siteName.endsWith("/")
            ? siteName.replaceAll("www.","")
            : siteName.replaceAll("www.","") + '/';
                
        Site site = (Site) INSTANCES.get(getUniqueName(shortSiteName,locale));
        if (site == null) {
            site = new Site(shortSiteName, locale);
        }
        return site;
    }

    // should never used except in catastrophe.
    private static final String SITE_DEFAULT_FALLBACK = "sesam.no";

    static {
        Locale.setDefault(new Locale("no","NO"));
        
        final Properties props = new Properties();
        try  {
            props.load(Site.class.getResourceAsStream("/" + SearchConstants.CONFIGURATION_FILE));
        }  catch (IOException ex) {
            LOG.fatal(FATAL_CANT_FIND_DEFAULT_SITE, ex);
        }
        final String defaultSiteName = props.getProperty(DEFAULT_SITE_KEY, SITE_DEFAULT_FALLBACK);

        DEFAULT = new Site(defaultSiteName, Locale.getDefault());
    }

    /** the default SiteSearch. For example: "sesam.no" or "localhost:8080".
     */
    public static final Site DEFAULT;

    public static String getUniqueName(final String siteName, final Locale locale) {
        return siteName+"["+locale.getDisplayName()+"]";
    }


}
