// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 *
 * @version $Id$
 */
@Controller("FieldEscapeHandler")
public final class FieldEscapeResultHandlerConfig extends AbstractResultHandlerConfig {

    private String sourceField;
    private String targetField;

    /**
     * 
     * @return 
     */
    public String getSourceField() {
        return sourceField;
    }

    /**
     * 
     * @param sourceField 
     */
    public void setSourceField(final String sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * 
     * @return 
     */
    public String getTargetField() {
        return targetField;
    }

    /**
     * 
     * @param targetField 
     */
    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        

        setSourceField(element.getAttribute("source-field"));
        setTargetField(element.getAttribute("target-field"));        
        
        return this;
    }

    
    
}
