/* Copyright (2006-2012) Schibsted ASA
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

import java.io.Serializable;
import java.util.Comparator;

/** A Modifier is a abstraction of a navigator model from a faceted search command's result.
 * The configuration bean to this class is the Navigator class.
 *
 * A facet item contains a name, a hitcount, and a refernence to the config Navigator bean.
 *
 * @version <tt>$Id$</tt>
 */
public final class Modifier implements Comparable<Modifier>, Serializable {

    private int count = 0;

    private final String name;
    private final Navigator navigator;

    private static final Comparator<Modifier> HINT_PRIO_COMPARATOR = new Comparator<Modifier>() {

        public int compare(final Modifier m1, final Modifier m2) {

            return 0; //FIXME restore
//            if (m1.getNavigationHint() == null || m2.getNavigationHint() == null) {
//                return 0;
//            }
//
//            int p1 = m1.getNavigationHint().getPriority();
//            int p2 = m2.getNavigationHint().getPriority();
//
//             if (p1 == p2) {
//                 return 0;
//             }
//
//             return p1 > p2 ? 1 : -1;
        }
    };

    public Modifier(final String name, final int count, final Navigator navigator) {
        this.name = name;
        this.count = count;
        this.navigator = navigator;
    }

    public Modifier(final String name, final Navigator navigator) {
        this.name = name;
        this.navigator = navigator;
    }


    public void addCount(final int count) {
        this.count += count;
    }

    public void subtractCount(final int count) {
        this.count -= count;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int compareTo(final Modifier other) {

        return Integer.valueOf(other.getCount()).compareTo(getCount());
    }

    @Override
    public String toString() {
        return name + '(' + getCount() + ')';
    }

    public static Comparator<Modifier> getHintPriorityComparator() {
        return HINT_PRIO_COMPARATOR;
    }
}
