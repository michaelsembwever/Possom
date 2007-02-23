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
 * @author maek
 */
public class CombineNavigatorsHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
    private static final String DEBUG_WRONG_RESULT_TYPE =
            "Can only be applied to fast search results";

    private Map<String, Set<String>> mappings = new HashMap();
    private String target;


    /** Creates a new instance of CombineNavigatorsHandler */
    public CombineNavigatorsHandler() {
    }

    /**
     * Combine the navigators that has been added using addMapping
     * into a new navigator.
     *
     * @param cxt The context.
     * @param parameters The parameters.
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {

        if (!(cxt.getSearchResult() instanceof FastSearchResult)) {
            LOG.debug(DEBUG_WRONG_RESULT_TYPE);
            return;
        }

        final FastSearchResult result = (FastSearchResult) cxt.getSearchResult();

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
                    result.addModifier(target, newMod);
                }
            }
        }

        if (result.getModifiers(target) != null) {
            Collections.sort(result.getModifiers(target));
        }
    }

    /**
     * Adds a navigator mapping where navigator is the source navigator and modifier is
     * the modifier to be used. (only modifiers explicitly added using this method will be added
     * to the new navigator).
     *
     * @param navigator A source navigator.
     * @param modifier The modifier name.
     */
    public void addMapping(final String navigator, final String modifier) {
        if (! mappings.containsKey(navigator))
            mappings.put(navigator, new HashSet());

        mappings.get(navigator).add(modifier);
    }

    /**
     * Sets the name of the target modifier.
     *
     * @param target The name of the target modifier.
     */
    public void setTarget(final String target) {
        this.target = target;
    }
}
