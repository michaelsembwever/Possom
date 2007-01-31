/*
 * StringDataObject.java
 *
 * Created on 23 January 2007, 12:43
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

import java.io.Serializable;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.*;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface StringDataObject extends Serializable{
    String getString();
    String getUtf8UrlEscaped();
    String getIso88591UrlEscaped();
    String getHtmlEscaped();
}
