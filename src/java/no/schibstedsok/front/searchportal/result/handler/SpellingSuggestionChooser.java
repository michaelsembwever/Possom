// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.spell.QuerySuggestion;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import no.schibstedsok.front.searchportal.result.SearchResult;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SpellingSuggestionChooser implements ResultHandler {

    private static transient Log log = LogFactory.getLog(SpellingSuggestionChooser.class);

    int minimumScore = -1;
    int maxSuggestions = 3;
    int maxDistance = 0;
    int muchBetter = 5;
    int maxSuggestionsForLongQueries = 2;
    int longQuery = 2;
    int veryLongQuery = 3;

    public SpellingSuggestionChooser() {
    }

    public SpellingSuggestionChooser(final int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public SpellingSuggestionChooser(final int minimumScore, final int maxSuggestions) {
        this.minimumScore = minimumScore;
        this.maxSuggestions = maxSuggestions;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        if (log.isDebugEnabled()) {
            log.debug("Number of corrected terms are " + numberOfCorrectedTerms(result.getSpellingSuggestions()));
        }

        final int numberOfTermsInQuery = result.getSearchCommand().getRunningQuery().getNumberOfTerms();

        if (numberOfTermsInQuery >= veryLongQuery && numberOfCorrectedTerms(result.getSpellingSuggestions()) > 1) {
            result.getSpellingSuggestions().clear();
        }

        for (final Iterator terms = result.getSpellingSuggestions().values().iterator(); terms.hasNext();) {
            final List suggestionList = (List) terms.next();

            Collections.sort(suggestionList);

            removeSuggestionsWithTooLowScore(suggestionList);
            limitNumberOfSuggestions(suggestionList, maxSuggestions);
            removeSuggestionsWithTooHighDifference(suggestionList);

            if (numberOfTermsInQuery >= longQuery) {
                if (numberOfCorrectedTerms(result.getSpellingSuggestions()) == 1) {
                    limitNumberOfSuggestions(suggestionList, maxSuggestionsForLongQueries);
                } else if (numberOfCorrectedTerms(result.getSpellingSuggestions()) == 2 && numberOfTermsInQuery < veryLongQuery) {
                    if (suggestionList.size() > 1) {
                        removeAllIfOneIsNotMuchBetter(suggestionList);
                    }
                }
            }

            if (suggestionList.isEmpty()) {
                terms.remove();
            }
        }

        final int numberOfCorrections = numberOfCorrectedTerms(result.getSpellingSuggestions());

        final String newQuery = result.getSearchCommand().getRunningQuery().getQueryString().toLowerCase(result.getSearchCommand().getRunningQuery().getLocale());

        if (numberOfCorrections == 1) {

            for (final Iterator suggestions = result.getSpellingSuggestions().values().iterator(); suggestions.hasNext();) {
                final List suggestionList = (List) suggestions.next();

                for (final Iterator iterator = suggestionList.iterator(); iterator.hasNext();) {
                    String query = newQuery;
                    String displayQuery = newQuery;
                    final SpellingSuggestion suggestion = (SpellingSuggestion) iterator.next();
                    query = query.replaceAll(suggestion.getOriginal(), suggestion.getSuggestion());
                    displayQuery = displayQuery.replaceAll(suggestion.getOriginal(), "<b>" + suggestion.getSuggestion() + "</b>");
                    result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
                }
            }
        } else if (numberOfCorrections == 2 && numberOfTermsInQuery < veryLongQuery) {
            String query = newQuery;
            String displayQuery = newQuery;

            for (final Iterator iterator = result.getSpellingSuggestions().values().iterator(); iterator.hasNext();) {
                final List suggestionList =  (List) iterator.next();

                for (final Iterator iterator1 = suggestionList.iterator(); iterator1.hasNext();) {
                    final SpellingSuggestion spellingSuggestion = (SpellingSuggestion) iterator1.next();
                    query = query.replaceAll(spellingSuggestion.getOriginal(), spellingSuggestion.getSuggestion());
                    displayQuery = displayQuery.replaceAll(spellingSuggestion.getOriginal(), "<b>" + spellingSuggestion.getSuggestion() + "</b>");
                }
            }
            result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
        }
    }

    private void removeAllIfOneIsNotMuchBetter(final List suggestionList) {
        final SpellingSuggestion best = (SpellingSuggestion) suggestionList.get(0);
        final SpellingSuggestion nextBest = (SpellingSuggestion) suggestionList.get(1);

        if (best.getScore() < nextBest.getScore() + muchBetter) {
            suggestionList.clear();
            if (log.isDebugEnabled()) {
                log.debug("All suggestions removed because the best is not much better than second best");
                log.debug("Best " + best);
                log.debug("Second best " + nextBest);
            }
        } else {
            suggestionList.clear();
            suggestionList.add(best);
            if (log.isDebugEnabled()) {
                log.debug("Only the best suggestion kept");
            }
        }
    }

    private int numberOfCorrectedTerms(final HashMap spellingSuggestions) {
        return spellingSuggestions.keySet().size();
    }

    private void removeSuggestionsWithTooHighDifference(final List suggestionList) {
        int lastScore = -1;

        for (final Iterator iterator = suggestionList.iterator(); iterator.hasNext();) {
            final SpellingSuggestion suggestion = (SpellingSuggestion) iterator.next();

            if (suggestion.getScore() + maxDistance < lastScore) {
                iterator.remove();
                log.debug("Suggestion " + suggestion + " because difference too high");

            } else {
                lastScore = suggestion.getScore();
            }
        }
    }

    private void limitNumberOfSuggestions(final List suggestionList, final int limit) {
        if (suggestionList.size() > limit) {
            final int numberToRemove = suggestionList.size() - limit;

            for (int i = 0; i < numberToRemove; i++) {
                final SpellingSuggestion removed = (SpellingSuggestion) suggestionList.remove(suggestionList.size() - 1);
                if (log.isDebugEnabled()) {
                    log.debug("Suggestion " + removed + " to reach maximum number of suggestions");
                }
            }
        }
    }

    private void removeSuggestionsWithTooLowScore(final List suggestionList) {
        for (final Iterator suggestions = suggestionList.iterator(); suggestions.hasNext();) {
            final SpellingSuggestion suggestion =  (SpellingSuggestion) suggestions.next();
            if (suggestion.getScore() < minimumScore) {
                suggestions.remove();
                if (log.isDebugEnabled()) {
                    log.debug("Suggestion " + suggestion + " removed due to low score");
                }
            }
        }
    }
}
