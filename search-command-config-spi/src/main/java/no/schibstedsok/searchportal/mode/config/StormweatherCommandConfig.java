// Copyright (2006-2007) Schibsted Søk AS
/*
 * NewsCommandConfig.java
 *
 * Created on March 7, 2006, 5:42 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;


/**
 *
 * @author larsj
 * @version $Id$
 */
@Controller("StormWeatherSearchCommand")
public final class StormweatherCommandConfig extends FastCommandConfig {
    
    private final List<String> elementValues = new ArrayList<String>();
        
    /**
     * 
     * @return 
     */
    public List<String> getElementValues() {
        return elementValues;
    }
    
    /**
     * 
     * @param string 
     */
    public void addElementValue(String string) {
        this.elementValues.add(string);
    }

    @Override
    public FastCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        if (element.getAttribute("xml-elements").length() > 0) {
            final String[] elms = element.getAttribute("xml-elements").split(",");
            for (String elm : elms) {
                addElementValue(elm.trim());
            }
        }

        // Add inherited xml elemts.
        if (inherit instanceof StormweatherCommandConfig) {
            final StormweatherCommandConfig swsi = (StormweatherCommandConfig) inherit;
            for (String elm : swsi.getElementValues()) {
                addElementValue(elm);
            }
        }

        return this;
    }
    
    
}
