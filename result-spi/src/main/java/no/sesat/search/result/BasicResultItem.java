/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.result;

import no.sesat.search.result.StringChopper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import no.sesat.search.result.StringChopper;

/**
 * A simple implementation of a search result item.
 * Is not multi-thread safe. 
 * Mutates on setter methods.
 * Delegates all fields (of all types) to the one map.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicResultItem implements ResultItem {
    
    private static final String URL_KEY = "url";
    private static final String TITLE_KEY = "title";

    private final HashMap<String,Serializable> fields = new HashMap<String,Serializable>();
    
    /**
     * 
     */
    public BasicResultItem(){}
    
    /**
     * 
     */
    protected BasicResultItem(final String title, final String url){
        
        fields.put(TITLE_KEY, StringChopper.chop(title, -1));
        fields.put(URL_KEY, StringChopper.chop(url, -1));
    }
    
    /**
     * 
     * @param copy 
     */
    public BasicResultItem(final ResultItem copy){
        
       for(String fieldName : copy.getFieldNames()){
           fields.put(fieldName, copy.getObjectField(fieldName));
       }
    }

    /**
     * 
     * @param field 
     * @param value 
     * @return 
     */
    public BasicResultItem addField(final String field, final String value) {

        fields.put(field, StringChopper.chop(value, -1));
        return this;
    }

    /**
     * 
     * @param field 
     * @return 
     */
    public String getField(final String field) {

        final String fieldValue = (String) fields.get(field);
        return fieldValue != null && fieldValue.trim().length() > 0 ? fieldValue : null;
    }

    /**
     * 
     * @param field 
     * @return 
     */
    public Serializable getObjectField(final String field) {

        return fields.get(field);
    }

    /**
     * 
     * @param field 
     * @param value 
     * @return 
     */
    public BasicResultItem addObjectField(final String field, final Serializable value) {
        
        fields.put(field, value);
        return this;
    }
    
    /**
     * 
     * @param field 
     * @return 
     */
    public Integer getInteger(final String field) {

        final String fieldValue = (String) fields.get(field);
        return null != fieldValue ? Integer.parseInt(fieldValue) : null;
    }

    /**
     * 
     * @param field 
     * @param maxLength 
     * @return 
     */
    public String getField(final String field, final int maxLength) {
        
        final String fieldValue = (String) fields.get(field);
        
        return fieldValue != null && fieldValue.trim().length() > 0
                ? StringChopper.chop(fieldValue, maxLength)
                : null;
    }

    /** Returns a defensive copy of the field names existing in this resultItem.
     * 
     * @return 
     */
    public Collection<String> getFieldNames() {

        return Collections.unmodifiableSet(fields.keySet());
    }

    /** Returns a live copy of the field's collection.
     * 
     * @param field 
     * @return 
     */
    public Collection<String> getMultivaluedField(final String field) {

        return (Collection<String>) fields.get(field);
    }

    /**
     * 
     * @param field 
     * @param value 
     * @return 
     */
    public BasicResultItem addToMultivaluedField(final String field, final String value) {
        
        if (! fields.containsKey(field)) {
            fields.put(field, new ArrayList<String>());
        }

        final Collection<String> previousValues = (Collection<String>) fields.get(field);
        previousValues.add(value);
        return this;
    }

    public boolean equals(final Object obj) {
        
        boolean result = false;
        if( obj instanceof ResultItem ){
            final ResultItem other = (ResultItem) obj;

            // FIXME very specific undocumented stuff here
            if (other.getField("recordid") != null && getField("recordid") != null) {
                result = getField("recordid").equals(other.getField("recordid"));
            }else{
                result = true;
                for(String fieldName : other.getFieldNames()){
                    if (other.getObjectField(fieldName) == null) {
                        result &= null == getObjectField(fieldName);
                    } else {
                        result &= other.getObjectField(fieldName).equals(getObjectField(fieldName));
                    }
                }
            }
        }else{
            result = super.equals(obj);
        }
        return result;
    }

    public int hashCode() {

        // FIXME very specific undocumented stuff here
        if (getField("recordid") != null) {
            return getField("recordid").hashCode();
            
        } else {
            // there nothing else to this object than the fields map.
            return fields.hashCode();
        }
    }

    public String getUrl() {

        return getField(URL_KEY);
    }

    public ResultItem setUrl(final String url) {

        return addField(URL_KEY, url);
    }

    public String getTitle() {

        return getField(TITLE_KEY);
    }

    public ResultItem setTitle(final String title) {

        return addField(TITLE_KEY, title);
    }

}