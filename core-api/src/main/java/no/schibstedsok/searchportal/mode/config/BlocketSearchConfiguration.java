package no.schibstedsok.searchportal.mode.config;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import no.schibstedsok.searchportal.query.transform.SynonymQueryTransformer;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;

/**
 * Configuration for the prisjakt command.
 */
public class BlocketSearchConfiguration extends AbstractSearchConfiguration {
    
	private  Map BLOCKETMAP;
	
	/**
     * Creates a new instance of this search configuration.
     */
    public BlocketSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    
    }
    
    public Map getBlocketMap()
    {
    	return BLOCKETMAP;
    }
    
    
    public void setBlocketMap(Map bmap)
    {
    	
    	BLOCKETMAP=bmap;
    
    }
    
    
   
    

            

        
            
}
    
