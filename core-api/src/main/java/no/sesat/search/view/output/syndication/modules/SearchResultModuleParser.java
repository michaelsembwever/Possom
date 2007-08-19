/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * SearchResultModuleParser.java
 *
 */

package no.sesat.search.view.output.syndication.modules;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;
import org.jdom.Element;

/**
 * A parser for the rome module defining the sesam syndication feed format.
 */
public class SearchResultModuleParser implements ModuleParser {

    /**
     * Creates a new instance of this class.
     */
    public SearchResultModuleParser() {
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespaceUri() {
        return SearchResultModule.URI;
    }

    /**
     * {@inheritDoc}
     */
    public Module parse(final Element root) {

        final SearchResultModule m = new SearchResultModuleImpl();

        boolean touched = false;

        final Element e = root.getChild(SearchResultModule.ELEM_NUMBER_OF_HITS, SearchResultModuleImpl.NS);

        if (e != null) {
            touched = true;
            m.setNumberOfHits(e.getText());
        }

        final Element ageElem = root.getChild(SearchResultModule.ELEM_ARTICLE_AGE, SearchResultModuleImpl.NS);

        if (ageElem != null) {
            touched = true;
            m.setArticleAge(e.getText());
        }

        final Element sourceElem = root.getChild(SearchResultModule.ELEM_NEWS_SOURCE, SearchResultModuleImpl.NS);

        if (sourceElem != null) {
            touched = true;
            m.setNewsSource(e.getText());
        }

        return touched == true ? m : null;
    }
}
