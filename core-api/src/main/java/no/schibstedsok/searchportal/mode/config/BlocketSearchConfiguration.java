package no.schibstedsok.searchportal.mode.config;


import java.util.Map;


/**
 * Configuration for the blocket command.
 */
public class BlocketSearchConfiguration extends AbstractSearchConfiguration {

	private  Map BLOCKETMAP;
    private static final String BLOCKET_CONFIGURATION_FILE = "blocket.properties";
    private static final String BLOCKET_SEARCH_WORDS_FILE = "blocket_search_words.xml";

	/**
     * Creates a new instance of this search configuration.
     */
    public BlocketSearchConfiguration(final SearchConfiguration sc) {
        super(sc);

    }

    public String getBlocketConfigFileName(){
    	return BLOCKET_SEARCH_WORDS_FILE;
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

