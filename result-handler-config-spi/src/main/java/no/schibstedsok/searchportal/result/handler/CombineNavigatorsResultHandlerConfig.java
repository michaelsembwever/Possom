// Copyright (2007) Schibsted Søk AS
/*
 * CombineNavigatorsHandler.java
 *
 */

package no.schibstedsok.searchportal.result.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * This class can be used to combine the modififers of two navigators into a new navigator.
 *
 * @author maek
 *  @version $Id$
 */
@Controller("CombineNavigatorsHandler")
public final class CombineNavigatorsResultHandlerConfig extends AbstractResultHandlerConfig {

    private static final Logger LOG = Logger.getLogger(CombineNavigatorsResultHandlerConfig.class);
    
    private final Map<String, Set<String>> mappings = new HashMap<String, Set<String>>();
    private String target;


    /** Creates a new instance of CombineNavigatorsHandler */
    public CombineNavigatorsResultHandlerConfig() {
    }

    /**
     * Adds a navigator mapping where navigator is the source navigator and modifier is
     * the modifier to be used. (only modifiers explicitly added using this method will be added
     * to the new navigator).
     *
     * @param navigator A source navigator.
     * @param modifier The modifier name.
     */
    public void addMapping(final String navigator, final String modifier) {
        
        if (! mappings.containsKey(navigator)){
            mappings.put(navigator, new HashSet<String>());
        }

        mappings.get(navigator).add(modifier);
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, Set<String>> getMappings(){
        
        return Collections.unmodifiableMap(mappings);
    }

    /**
     * Sets the name of the target modifier.
     *
     * @param target The name of the target modifier.
     */
    public void setTarget(final String target) {
        this.target = target;
    }
    
    /**
     * 
     * @return 
     */
    public String getTarget(){
        return target;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        setTarget(element.getAttribute("target"));

        final NodeList navs = element.getElementsByTagName("navigator");

        for (int i = 0; i < navs.getLength(); i++) {
            final Element nav = (Element) navs.item(i);

            final NodeList mods = nav.getElementsByTagName("modifier");
            for (int j = 0; j < mods.getLength(); j++) {
                final Element mod = (Element) mods.item(j);

                addMapping(nav.getAttribute("name"), mod.getAttribute("name"));
            }
        }      
        
        return this;
    }

    
}
