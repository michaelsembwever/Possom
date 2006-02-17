// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;

import java.util.Map;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AddDocCountModifier implements ResultHandler {

    private String modifierName;

    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        FastNavigator navigator = new FastNavigator();
        Modifier mod = new Modifier(modifierName, result.getHitCount(), navigator);
        if (mod.getCount() > 0)
            cxt.addSource(mod);
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(final String modifierName) {
        this.modifierName = modifierName;
    }
}
