/*
 * BrowserDataObject.java
 *
 * Created on 23 January 2007, 12:31
 *
 */

package no.schibstedsok.searchportal.datamodel.request;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;

/** Holds information regarding the user's browser.
 * Typically HTTP Request Headers.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface BrowserDataObject extends Serializable {
    StringDataObject getUserAgent();
    Locale getLocale();
    List<Locale> getSupportedLocales();
}
