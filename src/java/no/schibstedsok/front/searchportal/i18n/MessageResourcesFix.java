package no.schibstedsok.front.searchportal.i18n;

import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.PropertyMessageResources;

import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * This class fixes something that looks like a bug in struts (or in java.text.MessageFormat...?)
 * If the locale on messageformat is specified w/ setLocale instead of with the constructor, the locale is ignored.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MessageResourcesFix extends PropertyMessageResources {

    public MessageResourcesFix(MessageResourcesFactory messageResourcesFactory, String s) {
        super(messageResourcesFactory, s);
    }

    public String getMessage(Locale locale, String key, Object[] args) {
        // Cache MessageFormat instances as they are accessed
        if (locale == null) {
            locale = defaultLocale;
        }

        MessageFormat format = null;
        String formatKey = messageKey(locale, key);

        synchronized (formats) {
            format = (MessageFormat) formats.get(formatKey);
            if (format == null) {
                String formatString = getMessage(locale, key);

                if (formatString == null) {
                    return returnNull ? null : ("???" + formatKey + "???");
                }

                format = new MessageFormat(escape(formatString), locale); // Here is the fix
//                format.setLocale(locale); // This is ignored. 
                formats.put(formatKey, format);
            }
        }

       return format.format(args);
    }
}