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

import java.util.Map;
import no.sesat.search.result.Navigator;

/**
 *
 * @version $Id$
 */
public interface FacetedCommandConfig extends SearchConfiguration{

    /**
     *
     * @param navigatorKey
     * @return
     */
    Navigator getFacet(final String navigatorKey);

    /**
     *
     * @return
     */
    Map<String, Navigator> getFacets();

    /** Facet values (facets selected by the user) can be multi-valued using the specificated separator.
     * A blank string will disable any ability to select multiple facet values.
     *
     * The default separator is "," unless sepcified by implementation.
     * @return
     */
    String getFacetSeparator();
}
