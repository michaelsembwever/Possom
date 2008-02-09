/* Copyright (2007) Schibsted Søk AS
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
/*
 * StringDataObjectSupport.java
 *
 * Created on 23 January 2007, 12:43
 *
 */

package no.sesat.search.datamodel.generic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * This helper class provides a utility implementation of the
 * no.sesat.search.datamodel.StringDataObject interface.
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
        
        return null != getString() 
                ? StringEscapeUtils.escapeXml(getString()).replaceAll("&apos;", "&#39;") // see SEARCH-4057
                : null;
    }
    
    
}
