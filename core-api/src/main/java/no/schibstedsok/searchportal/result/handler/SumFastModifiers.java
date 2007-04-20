// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.util.Collections;
import java.util.Iterator;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class SumFastModifiers implements ResultHandler {

    private final SumResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public SumFastModifiers(final ResultHandlerConfig config){
        this.config = (SumResultHandlerConfig)config;
    }
    
    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final SearchResult result = cxt.getSearchResult();
        if (result.getHitCount() >= 0) {

            final FastSearchResult fastResult = (FastSearchResult) result;

            final Navigator navigator = fastResult.getNavigatedTo(config.getNavigatorName());

            final Modifier modifier = new Modifier(config.getTargetModifier(), navigator);

            if (fastResult.getModifiers(config.getNavigatorName()) != null) {

                for (Iterator iterator = fastResult.getModifiers(config.getNavigatorName()).iterator()
                        ; iterator.hasNext();) {
                    
                    final Modifier mod =  (Modifier) iterator.next();
                    
                    if (config.getModifierNames().contains(mod.getName())) {
                        modifier.addCount(mod.getCount());
                        iterator.remove();
                    }
                }
            }
            
            fastResult.addModifier(config.getNavigatorName(), modifier);
            Collections.sort(fastResult.getModifiers(config.getNavigatorName()));

        }
    }
}
