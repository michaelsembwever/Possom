// Copyright (2006) Schibsted Søk AS
/*
 * NewsSearchConfiguration.java
 *
 * Created on March 7, 2006, 5:42 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author larsj
 */
public class StormWeatherSearchConfiguration extends FastSearchConfiguration {
    
    private List<String> elementValues = new ArrayList<String>();
    
    public StormWeatherSearchConfiguration(){
        super(null);
    }
    
    public StormWeatherSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
    public List<String> getElementValues() {
        return elementValues;
    }
    
    public void addElementValue(String string) {
        this.elementValues.add(string);
    }
    
    
}
