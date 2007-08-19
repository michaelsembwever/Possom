/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * StringDataObjectSupport.java
 *
 * Created on 23 January 2007, 12:43
 *
 */

package no.sesat.searchportal.datamodel.generic;

import no.sesat.searchportal.datamodel.generic.DataObject;
import no.sesat.searchportal.datamodel.*;
import no.sesat.searchportal.datamodel.generic.StringDataObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This helper class provides a utility implementation of the
 * no.sesat.searchportal.datamodel.StringDataObject interface.
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
    
    /**
     * 
     * @param string 
     */
    public StringDataObjectSupport(final String string){
        this.string = string;
    }
    
    public String getString() {

        return string;
    }

    public String getUtf8UrlEncoded(){
        
        try {
            return null != getString() ? URLEncoder.encode(getString(), "UTF-8") : null;
            
        }catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("JVM doesn't support UTF-8 encoding", ex);
        }
    }
    
    public String getIso88591UrlEncoded(){
        
        try {
            return null != getString() ? URLEncoder.encode(getString(), "ISO-8859-1") : null;
            
        }catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("JVM doesn't support ISO-8859-1 encoding", ex);
        }
    }
    
    public String getXmlEscaped(){
        
        return null != getString() ? StringEscapeUtils.escapeXml(getString()) : null;
    }
    
    
}
