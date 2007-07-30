// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.view.config.SearchTab;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 * @deprecated SEARCH-2859
 */
public final class AddDocCountModifier implements ResultHandler {

    private static final String ERR_UNKNOWN_MODIFIER_1 = "NavigationHint does not exist for modifier ";

    private AddDocCountResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public AddDocCountModifier(final ResultHandlerConfig config){
        this.config = (AddDocCountResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {
        // FIXME work-in-progress on SEARCH-2859
    }

}
