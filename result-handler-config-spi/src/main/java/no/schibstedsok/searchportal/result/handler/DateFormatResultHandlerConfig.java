// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/**
 * DateFormatHandler is part of no.schibstedsok.searchportal.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
@Controller("DateFormatHandler")
public final class DateFormatResultHandlerConfig extends AbstractResultHandlerConfig {

    private String fieldPrefix = "";
    private String sourceField;

    /**
     * 
     * @return 
     */
    public String getFieldPrefix() {
        return fieldPrefix;
    }

    /**
     * 
     * @param fieldPrefix 
     */
    public void setFieldPrefix(final String fieldPrefix) {
        this.fieldPrefix = fieldPrefix;
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
    public String getSourceField() {
        return sourceField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);

        if (element.hasAttribute("prefix")) {
            setFieldPrefix(element.getAttribute("prefix"));
        }
        setSourceField(element.getAttribute("source"));        
        
        return this;
    }

    
}
