/* Copyright (2009) Schibsted Søk AS
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/** An extension of BasicResultList that provides navigators (which hold modifiers), and currentNavigators.
 *
 * @param <T> The each ResultItem classes that are used.
 * @version <tt>$Id$</tt>
 */
public class FacetedSearchResultImpl<T extends ResultItem> extends BasicResultList<T> implements FacetedSearchResult<T>{

    // Attributes ----------------------------------------------------

    private final Map<String,List<Modifier>> modifiers = new HashMap<String,List<Modifier>>();

    // Constructors --------------------------------------------------

    /**
     * Default constructor.
     */
    public FacetedSearchResultImpl() {}

    // Public --------------------------------------------------------

    public void addModifier(final String navigatorName, final Modifier modifier) {

        final List<Modifier> mods;

        if (!modifiers.containsKey(navigatorName)) {
            mods = new ArrayList<Modifier>();
            modifiers.put(navigatorName, mods);
        } else {
            mods = modifiers.get(navigatorName);
        }

        mods.add(modifier);
    }

    public List<Modifier> getModifiers(final String navigatorName) {
        return modifiers.get(navigatorName);
    }

    public Modifier getModifier(final String navigatorName, final String modifierName) {

        final List<Modifier> mods = getModifiers(navigatorName);

        if (mods != null) {
            for (final Iterator iterator = mods.iterator(); iterator.hasNext();) {
                final Modifier modifier = (Modifier) iterator.next();
                if (modifier.getName().equals(modifierName)) {
                    return modifier;
                }
            }
        }

        return null;
    }

    public int getModifierCount(final String navigatorName, final String modifierName) {

        final Modifier modifier = getModifier(navigatorName, modifierName);
        return modifier != null ?  modifier.getCount(): 0;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

}
