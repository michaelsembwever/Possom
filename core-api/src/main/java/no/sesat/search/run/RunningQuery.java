/* Copyright (2005-2007) Schibsted Søk AS
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
 * RunningQuery.java
 *
 * Created on 16 February 2006, 19:52
 *
 */

package no.sesat.search.run;

import java.util.List;
import java.util.Locale;
import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.query.Query;
import no.sesat.search.result.Modifier;
import no.sesat.search.view.config.SearchTab;

/** A RunningQuery is the central controller for a user's submitted search.
 * It has a one-to-one mapping to a search mode (see tabs.xml).
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface RunningQuery {

    public interface Context extends BaseContext, ResourceContext, DataModelContext {
        /** TODO comment me. **/
        SearchMode getSearchMode();
        /** TODO comment me. **/
        SearchTab getSearchTab();
    }

    /** TODO comment me. **/
    List getEnrichments();

    /**
     * First find out if the user types in an advanced search etc by analyzing the queryStr.
     * Then lookup correct tip using messageresources.
     *
     * @return user tip
     */
    String getGlobalSearchTips();

    /** TODO comment me. **/
    Locale getLocale();

    /** TODO comment me. **/
    Integer getNumberOfHits(final String configName);

    /** TODO comment me. **/
    Query getQuery();

    /** TODO comment me. **/
    SearchMode getSearchMode();

    /** TODO comment me. **/
    SearchTab getSearchTab();

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    void run() throws InterruptedException;

}
