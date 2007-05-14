// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;


/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Id$</tt>
 */
public final class DiscardDuplicatesResultHandler implements ResultHandler {

    private final List<String> keys = new ArrayList<String>();

    private final DiscardDuplicatesResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public DiscardDuplicatesResultHandler(final ResultHandlerConfig config){
        this.config = (DiscardDuplicatesResultHandlerConfig)config;
    }
    
    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (ResultItem searchResultItem : cxt.getSearchResult().getResults()) {

            String uniqueField = searchResultItem.getField(config.getSourceField()) + "";	//avoid nullpointers

            if(config.isDiscardCase()){
            	uniqueField = uniqueField.toLowerCase();
            }

            //remove entries with same name (not emtpy ones)
            if(uniqueField.length() > 0 && keys.contains(uniqueField)){
        		cxt.getSearchResult().removeResult(searchResultItem);
        	} else {
        		keys.add(uniqueField);
        	}
        }
    }

}