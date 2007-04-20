// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
@Controller("DiscardDuplicatesResultHandler")
public final class DiscardDuplicatesResultHandlerConfig extends AbstractResultHandlerConfig {

    private String sourceField;
    private boolean discardCase;

    /**
     * 
     * @param string 
     */
    public void setSourceField(final String string) {
        sourceField = string;
    }

    /**
     * 
     * @return 
     */
    public String getSourceField(){
        return sourceField;
    }
    
	/**
	 * 
	 * @return 
	 */
	public boolean isDiscardCase() {
		return discardCase;
	}

	/**
	 * 
	 * @param discardCase 
	 */
	public void setDiscardCase(boolean discardCase) {
		this.discardCase = discardCase;
	}

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        
        setSourceField(element.getAttribute("key"));
        setDiscardCase(new Boolean(element.getAttribute("ignorecase")));        
        
        return this;
    }

    
}