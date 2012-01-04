/* Copyright (2005-2012) Schibsted ASA
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
 * RunningQuery.java
 *
 * Created on 16 February 2006, 19:52
 *
 */

package no.sesat.search.run;

import no.sesat.commons.ioc.BaseContext;
import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.view.config.SearchTab;

/** A RunningQuery is the central controller for a user's submitted search.
 *
 * @version $Id$
 *
 *
 */
public interface RunningQuery {

    public interface Context extends BaseContext, ResourceContext, DataModelContext {
        /**
         *
         * @return
         */
        SearchMode getSearchMode();
        /**
         *
         * @return
         */
        SearchTab getSearchTab();
    }

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    void run() throws InterruptedException;

}
