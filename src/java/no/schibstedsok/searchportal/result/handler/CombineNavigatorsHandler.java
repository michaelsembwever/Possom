/*
 * ExtractNewsCountryHandler.java
 *
 * Created on June 15, 2006, 7:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.result.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;
import no.schibstedsok.searchportal.mode.config.FastNavigator;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.log4j.Logger;

/**
 *
 * @author maek
 */
public class CombineNavigatorsHandler implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
    private static final String DEBUG_WRONG_RESULT_TYPE =
            "Can only be applied to fast search results";
    
    private Map<String, Set<String>> mappings = new HashMap(); 
    private String target;

    
    /** Creates a new instance of ExtractNewsCountryHandler */
    public CombineNavigatorsHandler() {
    }

    public void handleResult(final Context cxt, final Map parameters) {

        if (!(cxt.getSearchResult() instanceof FastSearchResult)) {
            LOG.debug(DEBUG_WRONG_RESULT_TYPE);
            return;
        }
        
        final FastSearchResult result = (FastSearchResult) cxt.getSearchResult();

        for (final String nav : mappings.keySet()) {
            for (Iterator it = result.getModifiers(nav).iterator(); it.hasNext();) {
                Modifier mod = (Modifier) it.next();
            }

            for (final String mod : mappings.get(nav)) {
                final Modifier modifier = result.getModifier(nav, mod);

                if (modifier != null) {
                    final FastNavigator navigator = new FastNavigator();
                    final Modifier newMod = new Modifier(mod, modifier.getCount(), navigator);

                    newMod.setNavigationHint(cxt.getSearchTab().getNavigationHint(newMod.getName()));
                    result.addModifier(target, newMod);
                } 
                
            }
        }
        Collections.sort(result.getModifiers(target));
    }
        
    public void addMapping(final String navigator, final String modifier) {
        if (! mappings.containsKey(navigator))
            mappings.put(navigator, new HashSet());

        mappings.get(navigator).add(modifier);
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    private class Mapping {
        private final String navigator;
        private final String modifier;
        private final String message;
        
        public Mapping(final String navigator, final String modifier, final String message) {
            this.navigator = navigator;
            this.modifier = modifier;
            this.message = message;
        }
    }
}
