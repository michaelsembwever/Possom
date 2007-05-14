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
    
    /** The URL this result item represents.
     * 
     * @return 
     */
    String getUrl();
    
    /** Sets the URL to the returned instance which is otherwise equal to this.
     * There is no guarantee that this instance is altered.
     * This allows implementations to be immutable if they choose to be.
     * 
     * @param url 
     * @return 
     */
    ResultItem setUrl(String url);

    /**
     * 
     * @param field 
     * @return 
     */
    String getField(String field);
    
    /** Adds the field to the returned instance which is otherwise equal to this.
     * There is no guarantee that this instance is altered.
     * This allows implementations to be immutable if they choose to be.
     * 
     * @param name 
     * @param value 
     * @return 
     */
    ResultItem addField(String name, String value);
    
    /**
     * 
     * @param field 
     * @return 
     */
    Serializable getObjectField(String field);
    
    /** Adds the field to the returned instance which is otherwise equal to this.
     * There is no guarantee that this instance is altered.
     * This allows implementations to be immutable if they choose to be.
     * 
     * @param field 
     * @param value 
     * @return 
     */
    ResultItem addObjectField(String field, Serializable value);
    
    
    /** An unmodifiable copy of the multivalued field collection.
     * 
     * @param field 
     * @return 
     */
    public Collection<String> getMultivaluedField(String field);
    
    /** Adds (to the multivalued) field to the returned instance which is otherwise equal to this.
     * There is no guarantee that this instance is altered.
     * This allows implementations to be immutable if they choose to be.
     * @param field 
     * @param value 
     * @return 
     */
    public ResultItem addToMultivaluedField(String field, String value);
    
    /** An unmodifiable list of the field names.
     * 
     * @return 
     */
    Collection<String> getFieldNames();
}
