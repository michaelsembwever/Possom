// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.result.BasicResultList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.mode.command.FastSearchCommand;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.result.Navigator;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class FastSearchResult<T extends ResultItem> extends BasicResultList<T>{

    /** @deprecated will be removed without replacement in future version. **/
    final SearchCommand command;
    private Map<String,List<Modifier>> navigators = new HashMap<String,List<Modifier>>();
    private Map<String,Navigator> currentNavigators = new HashMap<String,Navigator>();


    /**
     * 
     * @param command 
     */
    public FastSearchResult(final SearchCommand command) {
        super();
        this.command = command;
    }

    /**
     * @deprecated will be removed without replacement in future version.
     * @param navigatorName 
     * @return 
     */
    public Navigator getNavigatedTo(final String navigatorName) {
        return ((FastSearchCommand)command).getNavigatedTo(navigatorName);
    }

    /**
     * @deprecated will be removed without replacement in future version.
     * @return 
     */
    public Map<String,Navigator> getNavigatedTo() {
        return ((FastSearchCommand)command).getNavigatedTo();
    }

    /**
     * @deprecated will be removed without replacement in future version.
     * @return 
     */
    public SearchCommand getSearchCommand() {
        return command;
    }
    
    /**
     * 
     * @param navigatorName 
     * @param modifier 
     */
    public void addModifier(final String navigatorName, final Modifier modifier) {

        final List<Modifier> modifiers;

        if (!navigators.containsKey(navigatorName)) {
            modifiers = new ArrayList<Modifier>();
            navigators.put(navigatorName, modifiers);
        } else {
            modifiers = (List<Modifier>) navigators.get(navigatorName);
        }

        modifiers.add(modifier);
    }


    /**
     * 
     * @param navigatorName 
     * @return 
     */
    public List<Modifier> getModifiers(final String navigatorName) {
        return (List<Modifier>) navigators.get(navigatorName);
    }

    /**
     * 
     * @param navigatorName 
     * @param modifierName 
     * @return 
     */
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

    /**
     * 
     * @param navigatorName 
     * @param modifierName 
     * @return 
     */
    public int getModifierCount(final String navigatorName, final String modifierName) {
        final Modifier modifier = getModifier(navigatorName, modifierName);

        if (modifier != null) {
            return modifier.getCount();
        } else {
            return 0;
        }
    }

    /**
     * 
     * @param currentNavigator 
     * @param navKey 
     */
    public void addCurrentNavigator(final Navigator currentNavigator, final String navKey) {
        currentNavigators.put(navKey, currentNavigator);
    }

    /**
     * 
     * @param navigatorName 
     * @return 
     */
    public Navigator getCurrentNavigator(final String navigatorName) {
        return (Navigator) currentNavigators.get(navigatorName);
    }

}
