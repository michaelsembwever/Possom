/*
 * Suggestion.java
 * 
 * Created on 10/05/2007, 13:20:03
 * 
 */

package no.sesat.searchportal.result;

import java.io.Serializable;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface Suggestion extends Serializable{

    /**
     * 
     * @return 
     */
    String getOriginal();
    
    /**
     * 
     * @return 
     */
    String getSuggestion();
    
    /**
     * 
     * @return 
     */
    String getHtmlSuggestion();
}
