// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.Navigator;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.view.config.SearchTab;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
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

        final SearchResult result = cxt.getSearchResult();
        final Navigator navigator = new Navigator();
        final SearchTab.NavigatorHint hint = cxt.getSearchTab().getNavigationHint(config.getModifierName());
        if( hint != null ){
            final Modifier mod = new Modifier(hint.getName(), result.getHitCount(), navigator);
            cxt.addSource(mod);
        }else{
            throw new IllegalStateException(ERR_UNKNOWN_MODIFIER_1 + config.getModifierName());
        }
    }

}
