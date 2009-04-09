/* Copyright (2006-2009) Schibsted ASA
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
package no.sesat.search.result;


import java.util.HashMap;
import java.util.Map;


/** An override of BasicResultList that provides navigators (which hold modifiers), and currentNavigators.
 *
 * @deprecated Nothing Fast specific here. Use instead FacetedSearchResultImpl.
 *
 * @param <T> The each ResultItem classes that are used.
 * @version <tt>$Id$</tt>
 */
public class FastSearchResult<T extends ResultItem> extends FacetedSearchResultImpl<T>{

    private Map<String,Navigator> currentNavigators = new HashMap<String,Navigator>();

    /** Add a current Navigator
     *
     * @param currentNavigator
     * @param navKey
     */
    public void addCurrentNavigator(final Navigator currentNavigator, final String navKey) {
        currentNavigators.put(navKey, currentNavigator);
    }

    /** Get the current navigator with name "navigatorName".
     *
     * @param navigatorName
     * @return
     */
    public Navigator getCurrentNavigator(final String navigatorName) {
        return currentNavigators.get(navigatorName);
    }

}
