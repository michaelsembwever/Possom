// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SumFastModifiers implements ResultHandler {

    private String targetModifier;
    private String navigatorName;
    private Collection modifierNames = new ArrayList();

    public SumFastModifiers(final String targetModifier, final String navigatorName) {
        this.targetModifier = targetModifier;
        this.navigatorName = navigatorName;
    }

    public void addModifierName(final String modifierName) {
        modifierNames.add(modifierName);
    }

    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        if (result.getHitCount() >= 0) {

            final FastSearchResult fastResult = (FastSearchResult) result;

            final FastNavigator navigator = fastResult.getNavigatedTo(navigatorName);

            final Modifier modifier = new Modifier(targetModifier, navigator);

            if (fastResult.getModifiers(navigatorName) != null) {

                for (final Iterator iterator = fastResult.getModifiers(navigatorName).iterator(); iterator.hasNext();) {
                    final Modifier mod =  (Modifier) iterator.next();
                    if (modifierNames.contains(mod.getName())) {
                        modifier.addCount(mod.getCount());
                        iterator.remove();
                    }
                }
            }
            fastResult.addModifier(navigatorName, modifier);
            Collections.sort(fastResult.getModifiers(navigatorName));

        }
    }
}
