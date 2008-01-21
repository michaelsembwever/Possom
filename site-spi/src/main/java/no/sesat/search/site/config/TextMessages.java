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

import no.sesat.search.site.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.ContextWrapper;

import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;


/** Wrapper to the MessageResources for a corresponding site.
 * The site to use is defined through the context.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class TextMessages {

    public interface Context extends SiteContext, PropertiesContext { };

    private static final Logger LOG = Logger.getLogger(TextMessages.class);

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
        TextMessages instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            instance = new TextMessages(cxt);
        }
        return instance;
    }

    /**
     * Utility wrapper to the instanceOf(Context).
     */
    public static TextMessages valueOf(final Site site) {

        // TextMessages.Context for this site & UrlResourceLoader.
        final TextMessages tm = TextMessages.valueOf(new TextMessages.Context() {
            public Site getSite() {
                return site;
            }
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {

                return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }

        });
        return tm;
    }


    private final Context context;
    private final Properties keys = new Properties();

    private TextMessages(final Context cxt) {

        LOG.trace("TextMessages(cxt)");
        try{
            INSTANCES_LOCK.writeLock().lock();
            context = cxt;

            // import browser-applicable text messages
            loadKeys(cxt.getSite().getLocale());

            // import messages from site's preferred locale [will not override already loaded messages]
            final SiteConfiguration siteConf
                    =  SiteConfiguration.instanceOf(ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));
            final String defaultLocale = siteConf.getProperty(SiteConfiguration.SITE_LOCALE_DEFAULT);

            assert null != defaultLocale
                    : SiteConfiguration.SITE_LOCALE_DEFAULT + " null in "
                    + context.getSite() + ' ' + siteConf.getProperties();

            final String[] prefLocale = defaultLocale.split("_");


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
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
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
                = context.newPropertiesLoader(
                context,
                MESSAGE_RESOURCE + "_" + locale.toString() + ".properties",
                keys);
        loader.abut();
        //loader.getProperties();
    }


    /** TODO comment me. **/
    public boolean hasMessage(final String key) {
        return keys.containsKey(key);
    }

    /** TODO comment me. **/
    public String getMessage(final String key) {
        return getMessageImpl(key);
    }

    /** TODO comment me. **/
    public String getMessage(final String key, final Object arg0) {
        return getMessageImpl(key, arg0);
    }

    /** TODO comment me. **/
    public String getMessage(final String key, final Object arg0, final Object arg1) {
        return getMessageImpl(key, arg0, arg1);
    }

    /** TODO comment me. **/
    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2) {
        return getMessageImpl(key, arg0, arg1, arg2);
    }

    /** TODO comment me. **/
    public String getMessage(final String key, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
        return getMessageImpl(key, arg0, arg1, arg3);
    }

    /** TODO comment me. **/
    public String getMessage(final String key, final Object... arguments){
        return getMessageImpl(key, arguments);
    }

    private String getMessageImpl(final String key, final Object... arguments){

        if(key == null || key.trim().length() == 0){
            return "";
        }else{
            // XXX Struts caches the MessageFormats. Is constructing a MessageFormat really slower than the synchronization?
            final String pattern = keys.getProperty(key);
            if(pattern == null){
                // make it visible that this key is not being localised!
                return "KEY: " + key;
            }else{
                final MessageFormat format = new MessageFormat(pattern, context.getSite().getLocale());
                return format.format(arguments);
            }
        }
    }

}
