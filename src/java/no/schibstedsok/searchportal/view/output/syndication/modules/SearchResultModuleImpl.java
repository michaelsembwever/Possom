/*
 * SearchResultModuleImpl.java
 */

package no.schibstedsok.searchportal.view.output.syndication.modules;

import com.sun.syndication.feed.module.ModuleImpl;
import org.jdom.Namespace;

/**
 * @see SearchResultModule
 */
public class SearchResultModuleImpl extends ModuleImpl 
        implements SearchResultModule {

    public static final Namespace NS 
            = Namespace.getNamespace(SearchResultModule.PREFIX, SearchResultModule.URI);
    
    private String numberOfHits;

    /**
     * Creates a new instance of this class.
     */
    public SearchResultModuleImpl() {
        super(SearchResultModule.class, SearchResultModule.URI);
    }

    /**
     * @inherit
     */
    public Class getInterface() {
        return SearchResultModule.class;
    }

     /**
     * @inherit
     */
   public void copyFrom(final Object object) {
        SearchResultModule m = (SearchResultModule) object;
        setNumberOfHits(m.getNumberOfHits());
    }

    /**
     * @inherit
     */
    public String getNumberOfHits() {
        return numberOfHits;
    }

    /**
     * @inherit
     */
    public void setNumberOfHits(final String numberOfHits) {
        this.numberOfHits = numberOfHits;
    }
}
