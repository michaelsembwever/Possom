/* Copyright (2006-2008) Schibsted Søk AS
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


/** An override of BasicResultList that provides navigators (which hold modifiers), and currentNavigators.
 *
 * @param <T> The each ResultItem classes that are used.
 * @version <tt>$Id$</tt>
 */
public class FastSearchResult<T extends ResultItem> extends BasicResultList<T>{

    private Map<String,List<Modifier>> navigators = new HashMap<String,List<Modifier>>();
    private Map<String,Navigator> currentNavigators = new HashMap<String,Navigator>();


    /**
     * Default constructor.
     */
    public FastSearchResult() {}

    /** To the list of modifiers found in the navigators under the name "navigatorName"
     * add the modifier.
     *
     * @param navigatorName
     * @param modifier
     */
    public void addModifier(final String navigatorName, final Modifier modifier) {

        final List<Modifier> modifiers;

        if (!navigators.containsKey(navigatorName)) {
            modifiers = new ArrayList<Modifier>();
            navigators.put(navigatorName, modifiers);
        } else {
            modifiers = navigators.get(navigatorName);
        }

        modifiers.add(modifier);
    }


    /** A live copy of the modifiers found in the navigation map under the name "navigatorNam".
     *
     * @param navigatorName
     * @return
     */
    public List<Modifier> getModifiers(final String navigatorName) {
        return navigators.get(navigatorName);
    }

    /** The modifier with name "modifierName" from the modifier list
     * found in the navigation map under the name "navigatorNam".
     *
     * @param navigatorName
     * @param modifierName
     * @return the modifier found,or null
     */
    public Modifier getModifier(final String navigatorName, final String modifierName) {

        final List modifiers = getModifiers(navigatorName);

        if (modifiers != null) {
            for (final Iterator iterator = modifiers.iterator(); iterator.hasNext();) {
                final Modifier modifier = (Modifier) iterator.next();
                if (modifier.getName().equals(modifierName)) {
                    return modifier;
                }
            }
        }

        return null;
    }

    /** The count of the modifier with name "modifierName" from the modifier list
     * found in the navigation map under the name "navigatorNam".
     *
     * @param navigatorName
     * @param modifierName
     * @return
     */
    public int getModifierCount(final String navigatorName, final String modifierName) {

        final Modifier modifier = getModifier(navigatorName, modifierName);
        return modifier != null ?  modifier.getCount(): 0;
    }

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
