/* Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
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
 * NavigationDataObject.java
 *
 * Created on 15/05/2007, 11:38:04
 *
 */

package no.sesat.search.datamodel.navigation;

import java.util.Map;
import no.sesat.search.datamodel.access.AccessAllow;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.view.navigation.NavigationConfig;
import no.sesat.search.result.NavigationItem;

/** Contains Navigation information.
 *
 *
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface NavigationDataObject {

    NavigationConfig getConfiguration();

    /**
     *
     * @return
     */
    Map<String,NavigationItem> getNavigations();

    /**
     *
     * @param key
     * @return
     */
    NavigationItem getNavigation(String key);

    /**
     * @param key
     * @param value
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, RUNNING_QUERY_HANDLING})
    void setNavigation(String key, NavigationItem value);

//    /**
//     *
//     * @return
//     */
//    @AccessAllow(VIEW_CONSTRUCTION)
//    List<NavigationItem> getHistory();
//
//    /**
//     *
//     * @param history
//     */
//    @AccessAllow({})
//    void setHistory(List<NavigationItem> history);

}
