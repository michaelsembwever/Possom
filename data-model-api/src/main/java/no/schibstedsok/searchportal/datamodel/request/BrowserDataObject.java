/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
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
