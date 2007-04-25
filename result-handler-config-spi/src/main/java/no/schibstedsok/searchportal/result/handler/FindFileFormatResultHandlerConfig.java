// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;


/**
 * @version itthkjer
 * @version $Id$
 */
@Controller("FindFileFormat")
public final class FindFileFormatResultHandlerConfig extends AbstractResultHandlerConfig {
    
    private String field;
    
    /**
     * 
     * @param field 
     */
    public void setField(final String field){
        this.field = field;
    }
    
    /**
     * 
     * @return 
     */
    public String getField(){
        return field;
    }

    public FindFileFormatResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        AbstractDocumentFactory.fillBeanProperty(this, null, "field", ParseType.String, element, null);
        return this;
    }

}
