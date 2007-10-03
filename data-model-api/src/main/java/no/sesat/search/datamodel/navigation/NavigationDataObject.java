/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * NavigationDataObject.java
 *
 * Created on 15/05/2007, 11:38:04
 *
 */

package no.sesat.search.datamodel.navigation;

import java.util.Map;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.view.navigation.NavigationConfig;
import no.sesat.search.result.NavigationItem;

/** Contains Navigation information.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface NavigationDataObject {

    NavigationConfig getConfiguration();

    /**
     *
     * @param key
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
    //@AccessAllow(ControlLevel.RUNNING_QUERY_RESULT_HANDLING)
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
