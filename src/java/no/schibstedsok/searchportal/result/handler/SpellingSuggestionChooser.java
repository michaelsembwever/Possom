// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.view.spell.QuerySuggestion;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SpellingSuggestionChooser implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(SpellingSuggestionChooser.class);

    /** TODO comment me. **/
    int minimumScore = -1;
    /** TODO comment me. **/
    int maxSuggestions = 3;
    /** TODO comment me. **/
    int maxDistance = 0;
    /** TODO comment me. **/
    int muchBetter = 5;
    /** TODO comment me. **/
    int maxSuggestionsForLongQueries = 2;
    /** TODO comment me. **/
    int longQuery = 2;
    /** TODO comment me. **/
    int veryLongQuery = 3;

    /** TODO comment me. **/
    public SpellingSuggestionChooser() {
    }

    /** TODO comment me. **/
    public SpellingSuggestionChooser(final int minimumScore) {
        this.minimumScore = minimumScore;
    }

    /** TODO comment me. **/
    public SpellingSuggestionChooser(final int minimumScore, final int maxSuggestions) {
        this.minimumScore = minimumScore;
        this.maxSuggestions = maxSuggestions;
    }

    /** @inherit **/
    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of corrected terms are " + numberOfCorrectedTerms(result.getSpellingSuggestions()));
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

        final String newQuery = cxt.getQueryString().toLowerCase(result.getSearchCommand().getRunningQuery().getLocale());

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

    private void removeAllIfOneIsNotMuchBetter(final List<SpellingSuggestion> suggestionList) {
        final SpellingSuggestion best =  suggestionList.get(0);
        final SpellingSuggestion nextBest =  suggestionList.get(1);

        if (best.getScore() < nextBest.getScore() + muchBetter) {
            suggestionList.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug("All suggestions removed because the best is not much better than second best");
                LOG.debug("Best " + best);
                LOG.debug("Second best " + nextBest);
            }
        } else {
            suggestionList.clear();
            suggestionList.add(best);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Only the best suggestion kept");
            }
        }
    }

    private int numberOfCorrectedTerms(final Map<String,List<SpellingSuggestion>> spellingSuggestions) {
        return spellingSuggestions.keySet().size();
    }

    private void removeSuggestionsWithTooHighDifference(final List<SpellingSuggestion> suggestionList) {
        int lastScore = -1;

        for (final Iterator<SpellingSuggestion> iterator = suggestionList.iterator(); iterator.hasNext();) {
            final SpellingSuggestion suggestion =  iterator.next();

            if (suggestion.getScore() + maxDistance < lastScore) {
                iterator.remove();
                LOG.debug("Suggestion " + suggestion + " because difference too high");

            } else {
                lastScore = suggestion.getScore();
            }
        }
    }

    private void limitNumberOfSuggestions(final List<SpellingSuggestion> suggestionList, final int limit) {
        if (suggestionList.size() > limit) {
            final int numberToRemove = suggestionList.size() - limit;

            for (int i = 0; i < numberToRemove; i++) {
                final SpellingSuggestion removed =  suggestionList.remove(suggestionList.size() - 1);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Suggestion " + removed + " to reach maximum number of suggestions");
                }
            }
        }
    }

    private void removeSuggestionsWithTooLowScore(final List<SpellingSuggestion> suggestionList) {
        for (final Iterator<SpellingSuggestion> suggestions = suggestionList.iterator(); suggestions.hasNext();) {
            final SpellingSuggestion suggestion =   suggestions.next();
            if (suggestion.getScore() < minimumScore) {
                suggestions.remove();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Suggestion " + suggestion + " removed due to low score");
                }
            }
        }
    }

    /** TODO comment me. **/
    public void setMinScore(final int i) {
        minimumScore = i;
    }

    /** TODO comment me. **/
    public void setMaxSuggestions(final int i) {
        maxSuggestions = i;
    }

    /** TODO comment me. **/
    public void setMaxDistance(final int i) {
        maxDistance = i;
    }

    /** TODO comment me. **/
    public void setMuchBetter(final int i) {
        muchBetter = i;
    }

    /** TODO comment me. **/
    public void setLongQuery(final int i) {
        longQuery = i;
    }

    /** TODO comment me. **/
    public void setVeryLongQuery(final int i) {
        veryLongQuery = i;
    }

    /** TODO comment me. **/
    public void setLongQueryMaxSuggestions(final int i) {
        maxSuggestionsForLongQueries = i;
    }
}
