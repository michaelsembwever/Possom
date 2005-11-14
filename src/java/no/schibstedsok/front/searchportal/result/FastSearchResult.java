package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.util.ScoreKeeper;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastSearchResult extends BasicSearchResult implements SearchResult {

    private HashMap navigators = new HashMap();
    private ScoreKeeper clusters = new ScoreKeeper();
    private Map currentNavigators = new HashMap();

    public FastSearchResult(SearchCommand command) {
        super(command);
    }

    public void addModifier(String navigatorName, Modifier modifier) {

        List modifiers;

        if (! navigators.containsKey(navigatorName)) {
            modifiers = new ArrayList();
            navigators.put(navigatorName, modifiers);
        } else {
            modifiers = (List) navigators.get(navigatorName);
        }

        modifiers.add(modifier);
    }


    public List getModifiers(String navigatorName) {
        return (List) navigators.get(navigatorName);
    }

    public Modifier getModifier(String navigatorName, String modifierName) {
        List modifiers = getModifiers(navigatorName);

        if (modifiers != null) {
            for (Iterator iterator = modifiers.iterator(); iterator.hasNext();) {
                Modifier modifier = (Modifier) iterator.next();
                if (modifier.getName().equals(modifierName)) {
                    return modifier;
                }
            }
        }

        return null;
    }

    public int getModifierCount(String navigatorName, String modifierName) {
        Modifier modifier = getModifier(navigatorName, modifierName);

        if (modifier != null) {
            return modifier.getCount();
        } else {
            return 0;
        }
    }

    public void addKeywordCluster(KeywordCluster cluster) {
        clusters.addScore(cluster);
    }

    public List getSortedClusters() {
        return clusters.getSortedByScore();
    }

    public void addCurrentNavigator(FastNavigator currentNavigator, String navKey) {
        currentNavigators.put(navKey, currentNavigator);
    }

    public FastNavigator getCurrentNavigator(String navigatorName) {
        return (FastNavigator) currentNavigators.get(navigatorName);
    }


}
