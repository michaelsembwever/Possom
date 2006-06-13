// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.schibstedsok.front.searchportal.result.SearchResultItem;


/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public class DiscardDuplicatesResultHandler implements ResultHandler {

    private String sourceField;
    private boolean discardCase;
    private final List<String> keys = new ArrayList<String>();
    
    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {

        	final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String uniqueField = searchResultItem.getField(sourceField) + "";	//avoid nullpointers
            
            if(isDiscardCase())
            	uniqueField = uniqueField.toLowerCase();
            
            //remove entries with same name (not emtpy ones)
            if(uniqueField.length() != 0 && keys.contains(uniqueField)){
        		iterator.remove();
        	} else {
        		keys.add(uniqueField);
        	} 
        }
    }

    public void setSourceField(final String string) {
        sourceField = string;
    }

	public boolean isDiscardCase() {
		return discardCase;
	}

	public void setDiscardCase(boolean discardCase) {
		this.discardCase = discardCase;
	}

}