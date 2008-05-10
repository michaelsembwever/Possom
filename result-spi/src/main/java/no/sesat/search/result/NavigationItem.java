/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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
 *
 * NavigationItem.java
 *
 * Created on 11/06/2007, 10:42:11
 *
 */

package no.sesat.search.result;

/**
 *
 *
 * @version $Id$
 */
public interface NavigationItem extends ResultList<NavigationItem>{

    boolean isSelected();
    void setSelected(boolean selected);

    boolean isChildSelected();
    NavigationItem getSelectedChild();
    NavigationItem getChildByTitle(String title);

    /**
     * The depth of this element. (Used when elements are organized in tree like struktures)
     * (This might not be the global depth of the navigation item)
     *
     * @return Depth of this element.
     */
    int getDepth();
}
