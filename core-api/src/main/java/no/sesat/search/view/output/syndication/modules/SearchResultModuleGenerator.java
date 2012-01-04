/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * SearchResultModuleGenerator.java
 */

package no.sesat.search.view.output.syndication.modules;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Syndication feed generator for the extended sesam feed syntax.
 */
public class SearchResultModuleGenerator implements ModuleGenerator {

    private static Set<Namespace> NAMESPACES;

    static {
        final Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(SearchResultModuleImpl.NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    /**
     * Creates a new instance of this class.
     */
    public SearchResultModuleGenerator() {
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
    public Set getNamespaces() {
        return NAMESPACES;
    }

    /**
     * {@inheritDoc}
     */
    public void generate(final Module module, final Element element) {

        // this is not necessary, it is done to avoid the namespace definition in every item.
        Element root = element;
        while (root.getParent() != null && root.getParent() instanceof Element) {
            root = (Element) element.getParent();
        }

        root.addNamespaceDeclaration(SearchResultModuleImpl.NS);

        final SearchResultModule m = (SearchResultModule) module;

        if (m.getNumberOfHits() != null) {
            element.addContent(generateSimpleElement(SearchResultModule.ELEM_NUMBER_OF_HITS, m.getNumberOfHits()));
        }

        if (m.getArticleAge() != null) {
            element.addContent(generateSimpleElement(SearchResultModule.ELEM_ARTICLE_AGE, m.getArticleAge()));
        }

        if (m.getNewsSource() != null) {
            element.addContent(generateSimpleElement(SearchResultModule.ELEM_NEWS_SOURCE, m.getNewsSource()));
        }
    }

    private Element generateSimpleElement(final String name, final String value) {
        final Element element = new Element(name, SearchResultModuleImpl.NS);
        element.addContent(value);
        return element;
    }
}
