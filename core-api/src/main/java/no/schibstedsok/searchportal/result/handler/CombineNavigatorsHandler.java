// Copyright (2007) Schibsted Søk AS
/*
 * CombineNavigatorsHandler.java
 *
 */

package no.schibstedsok.searchportal.result.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.log4j.Logger;

/**
 *
 * This class can be used to combine the modififers of two navigators into a new navigator.
 * 
 * Combine the navigators that has been added using addMapping into a new navigator.
 *
 * @author maek
 * @version $Id$
 */
public final class CombineNavigatorsHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
    private static final String DEBUG_WRONG_RESULT_TYPE = "Can only be applied to fast search results";

    private final CombineNavigatorsResultHandlerConfig config;
    
    /** Creates a new instance of CombineNavigatorsHandler 
     * @param config 
     */
    public CombineNavigatorsHandler(final ResultHandlerConfig config) {
        this.config = (CombineNavigatorsResultHandlerConfig)config;
    }

    /** {@inherit}
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {

        if (!(cxt.getSearchResult() instanceof FastSearchResult)) {
            LOG.debug(DEBUG_WRONG_RESULT_TYPE);
            return;
        }

        final FastSearchResult result = (FastSearchResult) cxt.getSearchResult();
        final Map<String, Set<String>> mappings = config.getMappings();

        for (final String nav : mappings.keySet()) {
            for (final String mod : mappings.get(nav)) {
                final Modifier modifier = result.getModifier(nav, mod);

                if (modifier != null) {
                    final Navigator navigator = new Navigator();
                    final Modifier newMod = new Modifier(mod, modifier.getCount(), navigator);
                    newMod.setNavigationHint(cxt.getSearchTab().getNavigationHint(newMod.getName()));
                    if (newMod.getName().equals("Norge")) {
                        newMod.subtractCount( result.getModifierCount("sources", "Mediearkivet") );
                    }
                    result.addModifier(config.getTarget(), newMod);
                }
            }
        }

        if (result.getModifiers(config.getTarget()) != null) {
            Collections.sort(result.getModifiers(config.getTarget()));
        }
    }

}
