// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.view.spell.QuerySuggestion;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;


/**
 *  Spelling suggestions are chosen in the following way:
 *
 * <ul>
 * <li>Discard all suggestions with score less than minimumScore</li>
 * <li>For each term, remove suggestions with lowest score so that the number
 *     of suggestions is less than maxSuggestions. If the query is long the limit is maxSuggestionsForLongQueries</li>
 * <li>Remove all suggestions whose score differs more than maxDistance from the suggestion with the highest score</li>
 * <li>If the query is long and if two terms have suggestions, remove all suggestions unless the best suggestion
 * is much better than the second best</li>
 * </ul>
 *
 * A new query is then created using the chosen suggestions.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class SpellingSuggestionChooser implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(SpellingSuggestionChooser.class);
    
    private final SpellingSuggestionChooserResultHandlerConfig config;

    /**
     * Create a new SpellingSuggestionChooser. 
     * @param config 
     */
    public SpellingSuggestionChooser(final ResultHandlerConfig config) {
        this.config = (SpellingSuggestionChooserResultHandlerConfig)config;
    }

    /**
     * {@inherit}
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final SearchResult result = cxt.getSearchResult();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of corrected terms are " + numberOfCorrectedTerms(result.getSpellingSuggestions()));
        }

        final int numberOfTermsInQuery = datamodel.getQuery().getQuery().getTermCount();

        if (numberOfTermsInQuery >= config.getVeryLongQuery() 
                && numberOfCorrectedTerms(result.getSpellingSuggestions()) > 1) {
            
            result.getSpellingSuggestions().clear();
        }

        for (Iterator<List<SpellingSuggestion>> terms = result.getSpellingSuggestions().values().iterator()
                ; terms.hasNext();) {
            
            final List<SpellingSuggestion> suggestionList = terms.next();

            Collections.sort(suggestionList);

            removeSuggestionsWithTooLowScore(suggestionList);
            limitNumberOfSuggestions(suggestionList, config.getMaxSuggestions());
            removeSuggestionsWithTooHighDifference(suggestionList);

            if (numberOfTermsInQuery >= config.getLongQuery()) {
                
                if (numberOfCorrectedTerms(result.getSpellingSuggestions()) == 1) {
                    
                    limitNumberOfSuggestions(suggestionList, config.getLongQueryMaxSuggestions());
                    
                } else if (numberOfCorrectedTerms(result.getSpellingSuggestions()) == 2 
                            && numberOfTermsInQuery < config.getVeryLongQuery()) {

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

        final String newQuery = datamodel.getQuery().getString().toLowerCase(datamodel.getSite().getSite().getLocale());

        if (numberOfCorrections == 1) {

            for (List<SpellingSuggestion> spellingSuggestions : result.getSpellingSuggestions().values()) {
                for (SpellingSuggestion suggestion : spellingSuggestions) {
                    String query = newQuery;
                    String displayQuery = newQuery;
                    query = query.replaceAll(suggestion.getOriginal(), suggestion.getSuggestion());
                    displayQuery = displayQuery
                            .replaceAll(suggestion.getOriginal(), "<b>" + suggestion.getSuggestion() + "</b>");
                    result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
                }
            }
        } else if (numberOfCorrections == 2 && numberOfTermsInQuery < config.getVeryLongQuery() ) {
            String query = newQuery;
            String displayQuery = newQuery;

            for (List<SpellingSuggestion> spellingSuggestions : result.getSpellingSuggestions().values()) {
                for (SpellingSuggestion spellingSuggestion : spellingSuggestions) {
                    query = query.replaceAll(spellingSuggestion.getOriginal(), spellingSuggestion.getSuggestion());
                    displayQuery = displayQuery.replaceAll(
                            spellingSuggestion.getOriginal(), 
                            "<b>" + spellingSuggestion.getSuggestion() + "</b>");
                }
            }
            result.addQuerySuggestion(new QuerySuggestion(query, displayQuery));
        }
    }

    private void removeAllIfOneIsNotMuchBetter(final List<SpellingSuggestion> suggestionList) {
        
        final SpellingSuggestion best = suggestionList.get(0);
        final SpellingSuggestion nextBest = suggestionList.get(1);

        if (best.getScore() < nextBest.getScore() + config.getMuchBetter()) {
            
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

    private int numberOfCorrectedTerms(final Map<String, List<SpellingSuggestion>> spellingSuggestions) {
        
        return spellingSuggestions.keySet().size();
    }

    private void removeSuggestionsWithTooHighDifference(final List<SpellingSuggestion> suggestionList) {
        
        int lastScore = -1;

        for (final Iterator<SpellingSuggestion> iterator = suggestionList.iterator(); iterator.hasNext();) {
            final SpellingSuggestion suggestion = iterator.next();

            if (suggestion.getScore() + config.getMaxDistance() < lastScore) {
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
                final SpellingSuggestion removed = suggestionList.remove(suggestionList.size() - 1);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Suggestion " + removed + " to reach maximum number of suggestions");
                }
            }
        }
    }

    private void removeSuggestionsWithTooLowScore(final List<SpellingSuggestion> suggestionList) {
        
        for (final Iterator<SpellingSuggestion> suggestions = suggestionList.iterator(); suggestions.hasNext();) {
            
            final SpellingSuggestion suggestion = suggestions.next();
            if (suggestion.getScore() < config.getMinScore()) {
                
                suggestions.remove();
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Suggestion " + suggestion + " removed due to low score");
                }
            }
        }
    }
}
