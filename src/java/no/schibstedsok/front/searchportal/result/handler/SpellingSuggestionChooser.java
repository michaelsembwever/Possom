package no.schibstedsok.front.searchportal.result.handler;

import no.schibstedsok.front.searchportal.spell.QuerySuggestion;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
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

    public SpellingSuggestionChooser(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public SpellingSuggestionChooser(int minimumScore, int maxSuggestions) {
        this.minimumScore = minimumScore;
        this.maxSuggestions = maxSuggestions;
    }

    public void handleResult(Context cxt, Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        if (log.isDebugEnabled()) {
            log.debug("Number of corrected terms are " + numberOfCorrectedTerms(result.getSpellingSuggestions()));
        }

        int numberOfTermsInQuery = result.getSearchCommand().getRunningQuery().getNumberOfTerms();

        if (numberOfTermsInQuery >= veryLongQuery && numberOfCorrectedTerms(result.getSpellingSuggestions()) > 1) {
            result.getSpellingSuggestions().clear();
        }

        for (Iterator terms = result.getSpellingSuggestions().values().iterator(); terms.hasNext();) {
            List suggestionList = (List) terms.next();

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

        int numberOfCorrections = numberOfCorrectedTerms(result.getSpellingSuggestions());

        String newQuery = result.getSearchCommand().getRunningQuery().getQueryString().toLowerCase(result.getSearchCommand().getRunningQuery().getLocale());

        if (numberOfCorrections == 1) {

            for (Iterator suggestions = result.getSpellingSuggestions().values().iterator(); suggestions.hasNext();) {
                List suggestionList = (List) suggestions.next();

                for (Iterator iterator = suggestionList.iterator(); iterator.hasNext();) {
                    String query = newQuery;
                    String displayQuery = newQuery;
                    SpellingSuggestion suggestion = (SpellingSuggestion) iterator.next();
                    query = query.replaceAll(suggestion.getOriginal(), suggestion.getSuggestion());
                    displayQuery = displayQuery.replaceAll(suggestion.getOriginal(), "<b>" + suggestion.getSuggestion() + "</b>");
                    result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
                }
            }
        } else if (numberOfCorrections == 2 && numberOfTermsInQuery < veryLongQuery) {
            String query = newQuery;
            String displayQuery = newQuery;

            for (Iterator iterator = result.getSpellingSuggestions().values().iterator(); iterator.hasNext();) {
                List suggestionList =  (List) iterator.next();

                for (Iterator iterator1 = suggestionList.iterator(); iterator1.hasNext();) {
                    SpellingSuggestion spellingSuggestion = (SpellingSuggestion) iterator1.next();
                    query = query.replaceAll(spellingSuggestion.getOriginal(), spellingSuggestion.getSuggestion());
                    displayQuery = displayQuery.replaceAll(spellingSuggestion.getOriginal(), "<b>" + spellingSuggestion.getSuggestion() + "</b>");
                }
            }
            result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
        }
    }

    private void removeAllIfOneIsNotMuchBetter(List suggestionList) {
        SpellingSuggestion best = (SpellingSuggestion) suggestionList.get(0);
        SpellingSuggestion nextBest = (SpellingSuggestion) suggestionList.get(1);

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

    private int numberOfCorrectedTerms(HashMap spellingSuggestions) {
        return spellingSuggestions.keySet().size();
    }

    private void removeSuggestionsWithTooHighDifference(List suggestionList) {
        int lastScore = -1;

        for (Iterator iterator = suggestionList.iterator(); iterator.hasNext();) {
            SpellingSuggestion suggestion = (SpellingSuggestion) iterator.next();

            if (suggestion.getScore() + maxDistance < lastScore) {
                iterator.remove();
                log.debug("Suggestion " + suggestion + " because difference too high");

            } else {
                lastScore = suggestion.getScore();
            }
        }
    }

    private void limitNumberOfSuggestions(List suggestionList, int limit) {
        if (suggestionList.size() > limit) {
            int numberToRemove = suggestionList.size() - limit;

            for (int i = 0; i < numberToRemove; i++) {
                SpellingSuggestion removed = (SpellingSuggestion) suggestionList.remove(suggestionList.size() - 1);
                if (log.isDebugEnabled()) {
                    log.debug("Suggestion " + removed + " to reach maximum number of suggestions");
                }
            }
        }
    }

    private void removeSuggestionsWithTooLowScore(List suggestionList) {
        for (Iterator suggestions = suggestionList.iterator(); suggestions.hasNext();) {
            SpellingSuggestion suggestion =  (SpellingSuggestion) suggestions.next();
            if (suggestion.getScore() < minimumScore) {
                suggestions.remove();
                if (log.isDebugEnabled()) {
                    log.debug("Suggestion " + suggestion + " removed due to low score");
                }
            }
        }
    }
}
