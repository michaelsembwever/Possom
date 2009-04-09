/* Copyright (2007) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * SearchResultModuleImpl.java
 */

package no.sesat.search.view.output.syndication.modules;

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
