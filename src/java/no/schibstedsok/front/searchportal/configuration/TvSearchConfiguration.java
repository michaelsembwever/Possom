/*
 * TvSearchConfiguration.java
 *
 * Created on 19 May 2006, 14:45
 *
 * @author ajamtli
 * @version $Id$
 */

package no.schibstedsok.front.searchportal.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for the TvSearchCommand
 *
 * @author andersjj
 */
public class TvSearchConfiguration extends FastSearchConfiguration {
    
    /** Filter for use when an empty query is sumbitted or no spesific sorting is used. **/
    private List<String> defaultChannels = new ArrayList<String>();
    
    /** Number of results to fetch for empty searches **/
    private int resultsToFetch;
    
    public TvSearchConfiguration() {
        super(null);
    }
    
    /** Creates a new instance of TvSearchConfiguration */
    public TvSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
    }
    
    public List<String> getDefaultChannels() {
        return defaultChannels;
    }
    
    public void setDefaultChannels(List<String> defaultChannels) {
        this.defaultChannels = defaultChannels;
    }
    
    public void addDefaultChannel(String defaultChannel) {
        defaultChannels.add(defaultChannel);
    }
    
    public int getResultsToFetch() {
        return resultsToFetch;
    }
    
    public void setResultsToFetch(int resultsToFetch) {
        this.resultsToFetch = resultsToFetch;
    }
}
