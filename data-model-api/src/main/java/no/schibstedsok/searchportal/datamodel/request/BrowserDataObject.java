// Copyright (2007) Schibsted Søk AS
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
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;

/** Holds information regarding the user's browser.
 * Typically HTTP Request Headers.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataNode
public interface BrowserDataObject extends Serializable {

    /**
     * 
     * @return 
     */
    StringDataObject getUserAgent();
    /**
     * 
     * @return 
     */
    StringDataObject getRemoteAddr();
    /**
     * 
     * @return 
     */
    StringDataObject getForwardedFor();
    /**
     * 
     * @return 
     */
    Locale getLocale();
    /**
     * 
     * @return 
     */
    List<Locale> getSupportedLocales();

}
