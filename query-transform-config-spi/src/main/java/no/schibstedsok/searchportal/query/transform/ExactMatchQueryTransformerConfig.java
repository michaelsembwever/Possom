// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;


/** Transforms the query into <br/>
 * field:^"query"$
 * or
 * ^"query"$ if field is null
 * <br/>
 *   Ensures that only an exact match is returned.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
@Controller("ExactMatchQueryTransformer")
public final class ExactMatchQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private String field;
    
    /**
     * 
     * @return 
     */
    public String getField(){
        return field;
    }
    
    /**
     * 
     * @param field 
     */
    public void setField(final String field){
        this.field = field;
    }
}
