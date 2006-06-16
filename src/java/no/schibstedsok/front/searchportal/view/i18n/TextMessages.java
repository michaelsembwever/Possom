// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.view.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesContext;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;

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
     */
    private static final Map<Site,TextMessages> INSTANCES = new HashMap<Site,TextMessages>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();



    private static final String DEBUG_LOADING_WITH_LOCALE = "Looking for "+MESSAGE_RESOURCE+"_";
    private static final String INFO_USING_DEFAULT_LOCALE = " is falling back to the default locale ";

    /** Find the correct instance handling this Site.
     **/
    public static TextMessages valueOf(final Context cxt) {

        final Site site = cxt.getSite();
        INSTANCES_LOCK.readLock().lock();
        TextMessages instance = INSTANCES.get(site);
        INSTANCES_LOCK.readLock().unlock();

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

        LOG.trace("TextMessages(cxt)");
        INSTANCES_LOCK.writeLock().lock();
        context = cxt;

        // import browser-applicable text messages
        loadKeys(cxt.getSite().getLocale());

        // import messages from site's preferred locale [will not override already loaded messages]
        final String[] prefLocale = SiteConfiguration.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class, cxt))
                .getProperty(SiteConfiguration.SITE_LOCALE_DEFAULT).split("_");


        switch(prefLocale.length){
            case 1:
                LOG.info(cxt.getSite()+INFO_USING_DEFAULT_LOCALE
                        + prefLocale[0]);
                loadKeys(new Locale(prefLocale[0]));
                break;
            case 2:
                LOG.info(cxt.getSite()+INFO_USING_DEFAULT_LOCALE
                        + prefLocale[0] + '_' + prefLocale[1]);
                loadKeys(new Locale(prefLocale[0],prefLocale[1]));
                break;
            case 3:
                LOG.info(cxt.getSite()+INFO_USING_DEFAULT_LOCALE + prefLocale[0]
                        + '_' + prefLocale[1] + '_' + prefLocale[2]);
                loadKeys(new Locale(prefLocale[0],prefLocale[1],prefLocale[2]));
                break;
        }


        INSTANCES.put(cxt.getSite(),this);
        INSTANCES_LOCK.writeLock().unlock();
    }

    private void loadKeys(final Locale l) {

        // import the variant-specific text messages [does not override existing values]
        LOG.debug(DEBUG_LOADING_WITH_LOCALE + l.getLanguage() + "_" + l.getCountry() + "_" + l.getVariant());
        performLoadKeys(l);

        // import the country-specific text messages [does not override existing values]
        LOG.debug(DEBUG_LOADING_WITH_LOCALE + l.getLanguage() + "_" + l.getCountry());
        performLoadKeys(new Locale(l.getLanguage(), l.getCountry()));

        // import the language-specifix text messages [does not override existing values]
        LOG.debug(DEBUG_LOADING_WITH_LOCALE + l.getLanguage());
        performLoadKeys(new Locale(l.getLanguage()));

    }

    private void performLoadKeys(final Locale locale) {

        final PropertiesLoader loader
                = context.newPropertiesLoader(MESSAGE_RESOURCE + "_" + locale.toString() + ".properties", keys);
        loader.abut();
        loader.getProperties();
    }

    public String getMessage(final String key) {
        return getMessageImpl(key);
    }

    public String getMessage(final String key, final Object arg0) {
        return getMessageImpl(key, arg0);
    }

    public String getMessage(final String key, final Object arg0, final Object arg1) {
        return getMessageImpl(key, arg0, arg1);
    }

    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2) {
        return getMessageImpl(key, arg0, arg1, arg2);
    }

    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
        return getMessageImpl(key, arg0, arg1, arg3);
    }

    public String getMessage(final String key, final Object... arguments){
        return getMessageImpl(key, arguments);
    }

    private String getMessageImpl(final String key, final Object... arguments){
        
        if( key == null || key.trim().length() == 0 ){
            return "";
        }else{
            // XXX Struts caches the MessageFormats. Is constructing a MessageFormat really slower than the synchronization?
            final String pattern = keys.getProperty(key);
            if( pattern == null ){
                // make it visible that this key is not being localised!
                return "KEY: " + key; 
            }else{
                final MessageFormat format = new MessageFormat(pattern, context.getSite().getLocale());
                return format.format(arguments);
            }
        }
    }

}
