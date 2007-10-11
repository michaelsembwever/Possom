/* Copyright (2007) Schibsted Søk AS
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
