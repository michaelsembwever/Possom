package no.schibstedsok.front.searchportal.result.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.result.ResultHandler;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SpellingSuggestionChooser;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;

import java.util.HashMap;
import java.util.List;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SpellingSuggestionChooserTest extends TestCase {

    public void testNoSuggestions() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        handleResult(chooser,result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    public void testOneTermOneSuggestion() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
    }

    public void testOneTermOneSuggestionwithLimit() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(230);
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser,result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    public void testOneTermTwoSuggestionsSameScore() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(2, numberOfSuggestions(result, "slankting"));
    }

    public void testOneTermNumberOfSuggestionsSameScoreOnLimit() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));
    }

    public void testOneTermNumberOfSuggestionsSameScoreOvertLimit() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));
    }

    public void testOneTermNumberOfSuggestionsDiffScoreOvertLimit() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 211);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 223);
        SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        SpellingSuggestion suggestion5 = new SpellingSuggestion("slankting", "slankinga", 227);
        SpellingSuggestion suggestion6 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        result.addSpellingSuggestion(suggestion5);
        result.addSpellingSuggestion(suggestion6);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));

        List suggestionList = (List) result.getSpellingSuggestions().get("slankting");

        assertFalse(suggestionList.contains(suggestion));
        assertFalse(suggestionList.contains(suggestion3));
    }

    public void testOneTermNumberOfSuggestionsSameScoreLimitToZero() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 0);
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser,result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    public void testOneTermTwoSuggestionsDifferentScore() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 230);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
        List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertFalse(suggestionList.contains(suggestion));
    }

    public void testTwoTermsBothWrong() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser,result);
        assertEquals(2, numberOfTermsCorrected(result));
        List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertTrue(suggestionList2.contains(suggestion2));
    }

    public void testTwoTermsBothWrongManySuggestions() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertNull(suggestionList2);
    }

    public void testTwoTermsBothWrongManySuggestionsOneMuchBetter() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 240);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser,result);
        assertEquals(2, numberOfTermsCorrected(result));
        List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertTrue(suggestionList2.contains(suggestion3));
    }

    public void testThreeTermsTwoWrong() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel bil"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 240);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser,result);
        assertEquals(0, numberOfTermsCorrected(result));
        assertEquals(0, result.getQuerySuggestions().size());
    }

    public void testThreeTermsOneWrong() {
        SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykkel bil"));
        SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser,result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, result.getQuerySuggestions().size());
    }


    private int numberOfTermsCorrected(BasicSearchResult result) {
        return result.getSpellingSuggestions().keySet().size();
    }

    private int numberOfSuggestions(BasicSearchResult result, String term) {
        List listOfSuggestions = (List) result.getSpellingSuggestions().get(term);
        return listOfSuggestions.size();
    }
    
    private void handleResult(final SpellingSuggestionChooser chooser, final SearchResult result){
        final ResultHandler.Context resultHandlerContext = new ResultHandler.Context(){
            public SearchResult getSearchResult() {
                return result;
            }

            public Site getSite() {
                return Site.DEFAULT;
            }

        };
        chooser.handleResult(resultHandlerContext, new HashMap());
    }
}
