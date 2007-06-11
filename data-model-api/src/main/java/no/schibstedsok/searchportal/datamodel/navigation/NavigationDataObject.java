/*
 * NavigationDataObject.java
 * 
 * Created on 15/05/2007, 11:38:04
 * 
 */

package no.schibstedsok.searchportal.datamodel.navigation;

import java.util.List;
import no.schibstedsok.searchportal.datamodel.search.*;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.mode.config.NavigationCommandConfig.Navigation;
import no.schibstedsok.searchportal.result.NavigationItem;

/** Contains Navigation information.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface NavigationDataObject {
    
//    NavigationConfiguration getConfiguration();
//    void setConfiguration(NavigationConfiguration configuration);
    
    /**
     * 
     * @return 
     */
    @AccessAllow(VIEW_CONSTRUCTION)
    Navigation getNavigation();
    
    /**
     * 
     * @param pager 
     */
    @AccessAllow(RUNNING_QUERY_RESULT_HANDLING)
    void setNavigation(Navigation pager);
    
    /**
     * 
     * @return 
     */
    @AccessAllow(VIEW_CONSTRUCTION)
    List<NavigationItem> getHistory();

    /**
     * 
     * @param history 
     */
    @AccessAllow({})
    void setHistory(List<NavigationItem> history);

    
    
}
