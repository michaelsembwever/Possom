/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * SearchResultModuleImpl.java
 */

package no.sesat.searchportal.view.output.syndication.modules;

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
    private String newsSource;
    
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

    /** {@inheritDoc} */
    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    /** {@inheritDoc} */
    public String getNewsSource() {
        return newsSource;
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
