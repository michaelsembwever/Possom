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
package no.sesat.search.result;

import java.util.List;

/** An extension of ResultList that stores the facets from a facet enabled search command.
 *
 * @version $Id$
 */
public interface FacetedSearchResult<T extends ResultItem> extends ResultList<T>{

    /**
     * To the list of modifiers found in the navigators under the name "navigatorName"
     * add the modifier.
     *
     * @param navigatorName
     * @param modifier
     */
    void addModifier(final String navigatorName, final Modifier modifier);

    /**
     * The modifier with name "modifierName" from the modifier list
     * found in the navigation map under the name "navigatorNam".
     *
     * @param navigatorName
     * @param modifierName
     * @return the modifier found,or null
     */
    Modifier getModifier(final String navigatorName, final String modifierName);

    /**
     * The count of the modifier with name "modifierName" from the modifier list
     * found in the navigation map under the name "navigatorNam".
     *
     * @param navigatorName
     * @param modifierName
     * @return
     */
    int getModifierCount(final String navigatorName, final String modifierName);

    /**
     * A live copy of the modifiers found in the navigation map under the name "navigatorName".
     *
     * @param navigatorName
     * @return
     */
    List<Modifier> getModifiers(final String navigatorName);

}
