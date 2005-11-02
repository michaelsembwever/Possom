package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AddDocCountModifier implements ResultHandler {

    private String modifierName;

    public void handleResult(SearchResult result, Map parameters) {
        FastNavigator navigator = new FastNavigator();
        Modifier mod = new Modifier(modifierName, result.getHitCount(), navigator);
        if (mod.getCount() > 0)
            result.getSearchCommand().getQuery().addSource(mod);
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }
}
