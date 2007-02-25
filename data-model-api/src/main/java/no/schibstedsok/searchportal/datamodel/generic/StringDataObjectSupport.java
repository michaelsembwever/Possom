/*
 * StringDataObjectSupport.java
 *
 * Created on 23 January 2007, 12:43
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.*;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This helper class provides a utility implementation of the
 * no.schibstedsok.searchportal.datamodel.StringDataObject interface.
 * </p>
 * <p>
 * Since this class directly implements the StringDataObject interface, the class
 * can, and is intended to be used either by subclassing this implementation,
 * or via ad-hoc delegation of an instance of this class from another.
 * </p>
 * 
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public class StringDataObjectSupport implements StringDataObject{
    
    private static final Logger LOG = Logger.getLogger(StringDataObjectSupport.class);
    
    private final String string;
    
    public StringDataObjectSupport(final String string){
        this.string = string;
    }
    
    public String getString() {

        return string;
    }

    public String getUtf8UrlEscaped(){
        
        try {
            return java.net.URLEncoder.encode(getString(), "UTF-8");
            
        }catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("JVM doesn't support UTF-8 encoding", ex);
        }
    }
    
    public String getIso88591UrlEscaped(){
        
        try {
            return java.net.URLEncoder.encode(getString(), "ISO-8859-1");
            
        }catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("JVM doesn't support ISO-8859-1 encoding", ex);
        }
    }
    
    public String getHtmlEscaped(){
        
        return StringEscapeUtils.escapeHtml(getString());
    }
    
    
}
