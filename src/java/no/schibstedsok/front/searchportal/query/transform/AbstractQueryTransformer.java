package no.schibstedsok.front.searchportal.query.transform;

import no.schibstedsok.front.searchportal.query.*;

/**
 * AbstractQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>

 * @vesrion $Revision$, $Author$, $Date$
 */
public abstract class AbstractQueryTransformer implements QueryTransformer {



    public String getFilter(Context cxt){
        return null;
    }
    
    public String getFilter(Context cxt, java.util.Map parameters) {
        return null;
    }
    
    public String getTransformedQuery(Context cxt) {
        return cxt.getQueryString();
    }
    
}
