// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;

/**
 * Transformes the query if the requestparameters contains a contentId.
 * 
 * @author Stian Hegglund
 * @version $Revision:$
 */
public class MapInfoPageQueryTransformer extends AbstractQueryTransformer {
      
    /** Required constructor. */
    public MapInfoPageQueryTransformer(final QueryTransformerConfig config){
       
    }
    
    /**
     * If the request parameteters contains the contentid parameter, append recordid to the query.
     * 
     * @see no.schibstedsok.searchportal.query.transform.QueryTransformer
     */
    public String getTransformedQuery() {
        final String originalQuery = getContext().getTransformedQuery();
        Map<String,StringDataObject> requestParameters = getContext().getDataModel().getParameters().getValues();
       
        if(requestParameters != null && requestParameters.containsKey("contentid")){
            return "recordid:" + requestParameters.get("contentid").getString();
        }
        
        return originalQuery; 
    }
}
