// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.mode.command.FastSearchCommand;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.view.spell.RelevantQuery;
import no.schibstedsok.searchportal.result.Navigator;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastSearchResult extends BasicSearchResult implements SearchResult {

    private HashMap navigators = new HashMap();
    private Map currentNavigators = new HashMap();
    private List relevantQueries = new ArrayList();

    public FastSearchResult(final SearchCommand command) {
        super(command);
    }

    public Navigator getNavigatedTo(final String navigatorName) {
        return ((FastSearchCommand) getSearchCommand()).getNavigatedTo(navigatorName);
    }

    public Map<String,Navigator> getNavigatedTo() {
        return ((FastSearchCommand) getSearchCommand()).getNavigatedTo();
    }
    
    public void addModifier(final String navigatorName, final Modifier modifier) {

        List modifiers;

        if (!navigators.containsKey(navigatorName)) {
            modifiers = new ArrayList();
            navigators.put(navigatorName, modifiers);
        } else {
            modifiers = (List) navigators.get(navigatorName);
        }

        modifiers.add(modifier);
    }


    public List<Modifier> getModifiers(final String navigatorName) {
        return (List<Modifier>) navigators.get(navigatorName);
    }

    public Modifier getModifier(final String navigatorName, final String modifierName) {
        final List modifiers = getModifiers(navigatorName);

        if (modifiers != null) {
            for (final Iterator iterator = modifiers.iterator(); iterator.hasNext();) {
                final Modifier modifier = (Modifier) iterator.next();
                if (modifier.getName().equals(modifierName)) {
                    return modifier;
                }
            }
        }

        return null;
    }

    public int getModifierCount(final String navigatorName, final String modifierName) {
        final Modifier modifier = getModifier(navigatorName, modifierName);

        if (modifier != null) {
            return modifier.getCount();
        } else {
            return 0;
        }
    }

    public void addCurrentNavigator(final Navigator currentNavigator, final String navKey) {
        currentNavigators.put(navKey, currentNavigator);
    }

    public Navigator getCurrentNavigator(final String navigatorName) {
        return (Navigator) currentNavigators.get(navigatorName);
    }

    public void addRelevantQuery(final RelevantQuery query) {
        relevantQueries.add(query);
    }

    /**
     * Get the relevantQueries.
     *
     * @return the relevantQueries.
     */
    public List getRelevantQueries() {
        Collections.sort(relevantQueries);
        return relevantQueries;
    }
}
