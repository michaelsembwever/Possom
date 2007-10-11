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
 * NavigationDataObject.java
 *
 * Created on 15/05/2007, 11:38:04
 *
 */

package no.sesat.search.datamodel.page;

import java.util.Map;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.AccessDisallow;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.view.config.SearchTab;

/** Contains Navigation information.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface PageDataObject {

    @AccessDisallow({DATA_MODEL_CONSTRUCTION})        
    SearchTab getCurrentTab();
    
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})    
    void setCurrentTab(SearchTab currentTab);

    /**
     *
     * @param key
     * @return
     */
    @AccessDisallow({DATA_MODEL_CONSTRUCTION})
    Map<String,SearchTab> getTabs();

    /**
     *
     * @param key
     * @return
     */
    @AccessDisallow({DATA_MODEL_CONSTRUCTION})
    SearchTab getTab(String tabName);
    
    @AccessAllow({})
    void setTab(String tabName, SearchTab tab);

}
