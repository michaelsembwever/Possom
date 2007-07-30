/*
 * NavigationDataObject.java
 *
 * Created on 15/05/2007, 11:38:04
 *
 */

package no.schibstedsok.searchportal.datamodel.page;

import java.util.Map;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.access.AccessDisallow;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.view.config.SearchTab;

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
