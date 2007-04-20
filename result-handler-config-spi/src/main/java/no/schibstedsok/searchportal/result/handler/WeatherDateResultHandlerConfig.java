// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/**
 * WeatherDateHandler is part of no.schibstedsok.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
@Controller("WeatherDateHandler")
public class WeatherDateResultHandlerConfig extends AbstractResultHandlerConfig  {


    private String targetField;
    
    /**
     * 
     */
    protected String sourceField;

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

    /**
     * 
     * @param sourceField 
     */
    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }
    
    /**
     * 
     * @return 
     */
    public String getSourceField(){
        return sourceField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        
        
        setTargetField(element.getAttribute("target"));
        setSourceField(element.getAttribute("source"));
        
        return this;
    }
    
    

}
