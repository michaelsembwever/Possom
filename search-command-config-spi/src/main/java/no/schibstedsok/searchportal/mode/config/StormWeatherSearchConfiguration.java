// Copyright (2006-2007) Schibsted Søk AS
/*
 * NewsSearchConfiguration.java
 *
 * Created on March 7, 2006, 5:42 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;


/**
 *
 * @author larsj
 * @version $Id$
 */
@Controller("StormWeatherSearchCommand")
public class StormWeatherSearchConfiguration extends FastSearchConfiguration {
    
    private final List<String> elementValues = new ArrayList<String>();
    
    /**
     * 
     */
    public StormWeatherSearchConfiguration(){
        super(null);
    }
    
    /**
     * 
     * @param asc 
     */
    public StormWeatherSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
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
