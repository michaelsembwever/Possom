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

    /** Creates a new instance of Site. */
    private Site(final String siteName) {
        // siteName must finish with a '\'
        this.siteName = siteName.endsWith("/")
            ? siteName
            : siteName + '/';
        this.cxtName = siteName.indexOf(':') >= 0
            ? siteName.substring(0, siteName.indexOf(':')) + "/" // don't include the port in the cxtName.
            : siteName;
        INSTANCES.put(siteName, this);
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
     * Getter for property siteName.
     * Same as name but without port specification.
     * Guaranteed to finish with '/'.
     * @return Value of property siteName.
     */
    public String getConfigContext() {
        return cxtName;
    }
    
    public String getCssDir(){
        return "http://" + siteName + cxtName + "css";
    }
    
    public String getJsDir(){
        return "http://" + siteName + cxtName + "javascript";
    }
    
    public String getImageDir(){
        return "http://" + siteName + cxtName + "images";
    }

    /** Get the instance for the given siteName.
     * A "www." prefix will be automatically ignored.
     * @param siteName the virtual host name.
     * @return the site bean.
     */
    public static Site valueOf(final String siteName) {

        final String shortSiteName = siteName.startsWith("www.")
            ? siteName.substring(4)
            : siteName;

        Site site = (Site) INSTANCES.get(shortSiteName);
        if (site == null) {
            site = new Site(shortSiteName);
        }
        return site;
    }

    // should never used except in catastrophe.
    private static final String SITE_DEFAULT_FALLBACK = "sesam.no";

    static {
        final Properties props = new Properties();
        try  {
            props.load(Site.class.getResourceAsStream("/"+SearchConstants.CONFIGURATION_FILE));
        }  catch (IOException ex) {
            LOG.fatal(FATAL_CANT_FIND_DEFAULT_SITE, ex);
        }
        final String defaultSiteName = props.getProperty(DEFAULT_SITE_KEY, SITE_DEFAULT_FALLBACK);

        DEFAULT = new Site(defaultSiteName);
    }

    /** the default SiteSearch. For example: "sesam.no" or "localhost:8080".
     */
    public static final Site DEFAULT;
}
