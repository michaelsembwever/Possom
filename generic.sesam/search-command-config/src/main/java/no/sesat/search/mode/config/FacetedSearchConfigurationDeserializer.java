/*
 * Copyright (2012) Schibsted ASA
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
 *
 */

package no.sesat.search.mode.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import no.sesat.search.result.Navigator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**  UTility class to deserialise facets.
 *
 * @version $Id$
 */
final class FacetedSearchConfigurationDeserializer {

    private static final Logger LOG = Logger.getLogger(FacetedSearchConfigurationDeserializer.class);

    private static final String ERR_ONLY_ONE_CHILD_FACET_ALLOWED
            = "Each Facet is only allowed to have one child. Parent was ";

    private static final String INFO_PARSING_FACET = "  Parsing facet ";

    /** Currently only used by the subclasses but hopefully open to all one day.
     * @param navsE w3c dom elements to deserialise
     * @return collection of Navigators
     */
    static final Collection<Navigator> parseNavigators(final Element navsE) {

        final Collection<Navigator> navigators = new ArrayList<Navigator>();
        final NodeList children = navsE.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            final Node child = children.item(i);
            if (child instanceof Element && "facet".equals(((Element) child).getTagName())) {
                final Element navE = (Element) child;
                final String id = navE.getAttribute("id");
                final String name = navE.getAttribute("name");
                final String sortAttr = navE.getAttribute("sort") != null && navE.getAttribute("sort").length() > 0
                        ? navE.getAttribute("sort").toUpperCase() : "COUNT";
                LOG.info(INFO_PARSING_FACET + id + " [" + name + "]" + ", sort=" + sortAttr);
                final Navigator.Sort sort = Navigator.Sort.valueOf(sortAttr);

                final boolean boundaryMatch = navE.getAttribute("boundary-match").equals("true");

                final Navigator nav = new Navigator(
                        name,
                        navE.getAttribute("field"),
                        navE.getAttribute("display-name"),
                        sort,
                        boundaryMatch);
                nav.setId(id);
                final Collection<Navigator> childNavigators = parseNavigators(navE);
                if (childNavigators.size() > 1) {
                    throw new IllegalStateException(ERR_ONLY_ONE_CHILD_FACET_ALLOWED + id);
                } else if (childNavigators.size() == 1) {
                    nav.setChildNavigator(childNavigators.iterator().next());
                }
                navigators.add(nav);
            }
        }

        return navigators;
    }

    static final void readNavigators(
            final Element element,
            final FacetedCommandConfig config,
            final SearchConfiguration inherit,
            final Map<String, Navigator> facets){

        final FacetedCommandConfig navCmdInherit = inherit instanceof FacetedCommandConfig
                ? (FacetedCommandConfig) inherit
                : null;

        if (null != navCmdInherit && null != navCmdInherit.getFacets()) {

            facets.putAll(navCmdInherit.getFacets());
        }

        final NodeList nList = element.getElementsByTagName("facets");

        for (int i = 0; i < nList.getLength(); ++i) {
            final Collection<Navigator> navs = parseNavigators((Element) nList.item(i));
            for (Navigator navigator : navs) {
                facets.put(navigator.getId(), navigator);
            }
        }
    }

    private FacetedSearchConfigurationDeserializer(){}

}
