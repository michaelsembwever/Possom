/*
 * ResultItem.java
 * 
 * Created on 10/05/2007, 12:56:22
 * 
 */

package no.schibstedsok.searchportal.result;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface ResultItem extends Serializable{

    /**
     * 
     * @param field 
     * @return 
     */
    Object getField(String field);
    
    /**
     * 
     * @return 
     */
    Collection<String> getFieldNames();
}
