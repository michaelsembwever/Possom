// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Map;
import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Configuration for the blocket command.
 * 
 * @version $Id$
 */
@Controller("BlocketSearchCommand")
public final class BlocketSearchConfiguration extends AbstractSearchConfiguration {

    private Map blocketMap;

    private static final String BLOCKET_SEARCH_WORDS_FILE = "blocket_search_words.xml";

    /**
     * Creates a new instance of this search configuration.
     * @param sc 
     */
    public BlocketSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }

    /**
     * 
     * @return 
     */
    public String getBlocketConfigFileName() {
        return BLOCKET_SEARCH_WORDS_FILE;
    }

    /**
     * 
     * @return 
     */
    public Map getBlocketMap() {
        return blocketMap;
    }

    /**
     * 
     * @param bmap 
     */
    public void setBlocketMap(final Map bmap) {
        blocketMap = bmap;
    }

}
