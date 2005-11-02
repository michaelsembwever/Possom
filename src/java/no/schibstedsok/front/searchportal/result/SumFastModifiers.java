package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.command.FastSearchCommand;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SumFastModifiers implements ResultHandler {

    private String targetModifier;
    private String navigatorName;
    private Collection modifierNames = new ArrayList();

    public SumFastModifiers(String targetModifier, String navigatorName) {
        this.targetModifier = targetModifier;
        this.navigatorName = navigatorName;
    }

    public void addModifierName(String modifierName) {
        modifierNames.add(modifierName);
    }

    public void handleResult(SearchResult result, Map parameters) {

        if (result.getHitCount() > 0 ) {

            FastSearchResult fastResult = (FastSearchResult) result;

            FastSearchCommand fastCommand = (FastSearchCommand) result.getSearchCommand();

            FastNavigator navigator = fastCommand.getNavigatedTo(navigatorName);

            Modifier modifier = new Modifier(targetModifier, navigator);

            if (fastResult.getModifiers(navigatorName) != null) {

                for (Iterator iterator = fastResult.getModifiers(navigatorName).iterator(); iterator.hasNext();) {
                    Modifier mod =  (Modifier) iterator.next();
                    if (modifierNames.contains(mod.getName())) {
                        modifier.addCount(mod.getCount());
                        iterator.remove();
                    }
                }

                if (modifier.getCount() > 0) {
                    fastResult.addModifier(navigatorName, modifier);
                    Collections.sort(fastResult.getModifiers(navigatorName));
                }
            }
        }
    }
}
