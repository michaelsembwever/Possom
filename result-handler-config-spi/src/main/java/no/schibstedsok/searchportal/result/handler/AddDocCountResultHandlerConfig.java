// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("AddDocCountModifier")
public final class AddDocCountResultHandlerConfig extends AbstractResultHandlerConfig {


    private String modifierName;

    /**
     * 
     * @return 
     */
    public String getModifierName() {
        return modifierName;
    }

    /**
     * 
     * @param modifierName 
     */
    public void setModifierName(final String modifierName) {
        this.modifierName = modifierName;
    }
    
    public AbstractResultHandlerConfig readResultHandler(final Element element){
        
        super.readResultHandler(element);
        setModifierName(element.getAttribute("modifier"));
        return this;
    }
}
