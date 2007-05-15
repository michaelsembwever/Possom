// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

/**
 * 
 * @author Stian Hegglund
 * @version $Revision:$
 */
@Controller("MapInfoPageQueryTransformer")
public class MapInfoPageQueryTransformerConfig extends AbstractQueryTransformerConfig{
    private String parameterName = "contentid";
    private String prefix = "recordid";
   
    public final String getParameterName() {
        return parameterName;
    }
    
    public final String getPrefix() {
        return prefix;
    }
    
    @Override
    public MapInfoPageQueryTransformerConfig readQueryTransformer(Element element) {
        if (element.hasAttribute("parameter-name")){
            this.parameterName = element.getAttribute("parameter-name");
        }
        
        if (element.hasAttribute("prefix")) {
            this.prefix = element.getAttribute("prefix");
        }
        
        return this;
    }
}
