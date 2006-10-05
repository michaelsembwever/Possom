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
    private String articleAge;

    /**
     * Creates a new instance of this class.
     */
    public SearchResultModuleImpl() {
        super(SearchResultModule.class, SearchResultModule.URI);
    }

    /**
     * {@inheritDoc}
     */
    public Class getInterface() {
        return SearchResultModule.class;
    }

    /**
     * {@inheritDoc}
     */
    public void copyFrom(final Object object) {
        SearchResultModule m = (SearchResultModule) object;
        setNumberOfHits(m.getNumberOfHits());
        setArticleAge(m.getArticleAge());
    }

    /**
     * {@inheritDoc}
     */
    public String getNumberOfHits() {
        return numberOfHits;
    }

    /**
     * {@inheritDoc}
     */
    public void setNumberOfHits(final String numberOfHits) {
        this.numberOfHits = numberOfHits;
    }

    /**
     * {@inheritDoc}
     */
    public String getArticleAge() {
        return articleAge;
    }

    /**
     * {@inheritDoc}
     */
    public void setArticleAge(final String articleAge) {
        this.articleAge = articleAge;
    }
}
