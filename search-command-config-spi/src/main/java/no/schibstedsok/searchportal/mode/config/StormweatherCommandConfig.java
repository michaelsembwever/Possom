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


/**
 *
 * @author larsj
 * @version $Id$
 */
@Controller("StormWeatherSearchCommand")
public class StormweatherCommandConfig extends FastCommandConfig {
    
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
    
    
}
