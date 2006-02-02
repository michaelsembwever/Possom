// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import no.schibstedsok.front.searchportal.configuration.loaders.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.PropertiesContext;
import no.schibstedsok.front.searchportal.configuration.loaders.UrlResourceLoader;

import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Wrapper to the MessageResources for a corresponding site.
 * The site to use is defined through the context.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class TextMessages {

    public interface Context extends SiteContext, PropertiesContext { };

    private static final Log LOG = LogFactory.getLog(TextMessages.class);

    private static final String MESSAGE_RESOURCE = "messages";

    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map/*<Site,TextMessages>*/ INSTANCES = new HashMap/*<Site,TextMessages>*/();

    private static final String DEBUG_LOADING_WITH_LOCALE = "Looking for "+MESSAGE_RESOURCE+"_";
    private static final String INFO_USING_DEFAULT_LOCALE = " is falling back to the default locale ";

    /** Find the correct instance handling this Site.
     **/
    public static TextMessages valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        TextMessages instance = (TextMessages) INSTANCES.get(site);
        if (instance == null) {
            instance = new TextMessages(cxt);
        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     */
    public static TextMessages valueOf(final Site site) {

        // TextMessages.Context for this site & UrlResourceLoader.
        final TextMessages tm = TextMessages.valueOf(new TextMessages.Context() {
            public Site getSite() {
                return site;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }

        });
        return tm;
    }


    private final Context context;
    private final Properties keys = new Properties();

    private TextMessages(final Context cxt) {
        context = cxt;

        if (!loadKeys(cxt.getSite().getLocale())) {
            LOG.info(cxt.getSite()+INFO_USING_DEFAULT_LOCALE+Locale.getDefault());
            loadKeys(Locale.getDefault());
        }
        
        INSTANCES.put(cxt.getSite(),this);
    }

    private boolean loadKeys(final Locale l) {
        LOG.debug(DEBUG_LOADING_WITH_LOCALE+l.getLanguage()+"_"+l.getCountry()+"_"+l.getVariant());
        if (!loadKeysFallback(l)) {
            // ignore variant
            LOG.debug(DEBUG_LOADING_WITH_LOCALE+l.getLanguage()+"_"+l.getCountry());
            if (!loadKeysFallback(new Locale(l.getLanguage(), l.getCountry()))) {
                // ignore country
                LOG.debug(DEBUG_LOADING_WITH_LOCALE+l.getLanguage());
                if (!loadKeysFallback(new Locale(l.getLanguage()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean loadKeysFallback(final Locale locale) {
        final PropertiesLoader loader 
                = context.newPropertiesLoader(MESSAGE_RESOURCE+"_"+locale.toString()+".properties", keys);
        loader.abut();
        loader.getProperties();
        return keys.size() > 0;
    }

    //// JDK1.4 methods

    public String getMessage(final String key, final Object[] args) {
        // XXX Struts caches the MessageFormats. Is constructing a MessageFormat really slower than the synchronization?
        final MessageFormat format = new MessageFormat(keys.getProperty(key), context.getSite().getLocale());
        return format.format(args);
    }

    public String getMessage(final String key) {
        return getMessage(key, new Object[]{});
    }

    public String getMessage(final String key, final Object arg0) {
        return getMessage(key, new Object[]{arg0});
    }

    public String getMessage(final String key, final Object arg0, final Object arg1) {
        return getMessage(key, new Object[]{arg0, arg1});
    }

    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2) {
        return getMessage(key, new Object[]{arg0, arg1, arg2});
    }

    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
        return getMessage(key, new Object[]{arg0, arg1, arg3});
    }

    //// JDK1.5 method
//    public String getMessage(final String key, final Object... arguments){
//        // XXX Struts caches the MessageFormats. Is constructing a MessageFormat really slower than the synchronization?
//        final MessageFormat format = new MessageFormat(keys.getProperty(key),context.getSite().getLocale());
//        return format.format(arguments);
//    }

}
