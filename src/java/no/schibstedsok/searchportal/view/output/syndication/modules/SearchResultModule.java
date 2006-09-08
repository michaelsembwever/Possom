/*
 * SearchResultModule.java
 */

package no.schibstedsok.searchportal.view.output.syndication.modules;

import com.sun.syndication.feed.module.Module;
import org.jdom.Namespace;

/**
 * This interface describes the additional fields defined in the sesam 
 * syndication feed format.
 */
public interface SearchResultModule extends Module {

    public static final String URI = "http://www.sesam.no/rss/ns/search/1.0";
    public static final String PREFIX = "sesam";
    
    /**
     * Returns the number of hits for the search.
     */
    String getNumberOfHits();

    /**
     * Sets the number of hits for the search.
     */
    void setNumberOfHits(String numberOfHits);
}
