// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("SumFastModifier")
public final class SumResultHandlerConfig extends AbstractResultHandlerConfig {

    private String targetModifier;
    private String navigatorName;
    private final Collection<String> modifierNames = new ArrayList<String>();

    /**
     * 
     * @param targetModifier 
     */
    public void setTargetModifier(final String targetModifier){
        this.targetModifier = targetModifier;
    }

    /**
     * 
     * @return 
     */
    public String getTargetModifier(){
        return targetModifier;
    }

    /**
     * 
     * @param nm 
     */
    public void setNavigatorName(final String nm){
        navigatorName = nm;
    }

    /**
     * 
     * @return 
     */
    public String getNavigatorName(){
        return navigatorName;
    }
    
    /**
     * 
     * @param modifierName 
     */
    public void addModifierName(final String modifierName) {
        modifierNames.add(modifierName);
    }

    /**
     * 
     * @return 
     */
    public Collection<String> getModifierNames(){
        
        return Collections.unmodifiableCollection(modifierNames);
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        
        final String[] modifiers = element.getAttribute("modifiers").split(",");
        for (String modifier : modifiers) {
            addModifierName(modifier);
        }
        setNavigatorName(element.getAttribute("navigation"));
        setTargetModifier(element.getAttribute("target"));

        return this;
    }
    
    

}
