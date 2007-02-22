// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooser;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.view.config.SearchTab;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;



/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SpellingSuggestionChooserTest extends SiteTestCase {

    public SpellingSuggestionChooserTest(final String testName) {
        super(testName);
    }
    
    @Test
    public void testNoSuggestions() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    @Test
    public void testOneTermOneSuggestion() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
    }

    @Test
    public void testOneTermOneSuggestionwithLimit() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(230);
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    @Test
    public void testOneTermTwoSuggestionsSameScore() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(2, numberOfSuggestions(result, "slankting"));
    }

    @Test
    public void testOneTermNumberOfSuggestionsSameScoreOnLimit() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));
    }

    @Test
    public void testOneTermNumberOfSuggestionsSameScoreOvertLimit() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        final SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));
    }

    @Test
    public void testOneTermNumberOfSuggestionsDiffScoreOvertLimit() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 3);
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 211);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 223);
        final SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        final SpellingSuggestion suggestion5 = new SpellingSuggestion("slankting", "slankinga", 227);
        final SpellingSuggestion suggestion6 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        result.addSpellingSuggestion(suggestion5);
        result.addSpellingSuggestion(suggestion6);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));

        final List suggestionList = (List) result.getSpellingSuggestions().get("slankting");

        assertFalse(suggestionList.contains(suggestion));
        assertFalse(suggestionList.contains(suggestion3));
    }

    @Test
    public void testOneTermNumberOfSuggestionsSameScoreLimitToZero() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(-1, 0);
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("slankting", "slankinga", 227);
        final SpellingSuggestion suggestion4 = new SpellingSuggestion("slankting", "slafnkinga", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    @Test
    public void testOneTermTwoSuggestionsDifferentScore() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 230);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
        final List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertFalse(suggestionList.contains(suggestion));
    }

    @Test
    public void testTwoTermsBothWrong() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result);
        assertEquals(2, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertTrue(suggestionList2.contains(suggestion2));
    }

    @Test
    public void testTwoTermsBothWrongManySuggestions() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertNull(suggestionList2);
    }

    @Test
    public void testTwoTermsBothWrongManySuggestionsOneMuchBetter() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 240);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result);
        assertEquals(2, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestions().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestions().get("sykel");
        assertTrue(suggestionList2.contains(suggestion3));
    }

    @Test
    public void testThreeTermsTwoWrong() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykel bil"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("sykel", "sykkel", 227);
        final SpellingSuggestion suggestion3 = new SpellingSuggestion("sykel", "sykkkel", 240);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
        assertEquals(0, result.getQuerySuggestions().size());
    }

    @Test
    public void testThreeTermsOneWrong() {
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser();
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting sykkel bil"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, result.getQuerySuggestions().size());
    }


    private int numberOfTermsCorrected(final BasicSearchResult result) {
        return result.getSpellingSuggestions().keySet().size();
    }

    private int numberOfSuggestions(final BasicSearchResult result, final String term) {
        final List listOfSuggestions = (List) result.getSpellingSuggestions().get(term);
        return listOfSuggestions.size();
    }

    private void handleResult(final SpellingSuggestionChooser chooser, final SearchResult result) {
        final ResultHandler.Context resultHandlerContext = new ResultHandler.Context() {
            public SearchResult getSearchResult() {
                return result;
            }

            public Site getSite() {
                return getTestingSite();
            }
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final Properties properties) {
                
                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public DocumentLoader newDocumentLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final DocumentBuilder builder) {
                
                return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }
            public String getQueryString() {
                return result.getSearchCommand().getRunningQuery().getQueryString();
            }

            public Query getQuery() {
                return result.getSearchCommand().getRunningQuery().getQuery();
            }

            public void addSource(final Modifier modifier) {
                result.getSearchCommand().getRunningQuery().addSource(modifier);
            }
            public SearchTab getSearchTab(){
                return result.getSearchCommand().getRunningQuery().getSearchTab();
            }

        };
        chooser.handleResult(resultHandlerContext, new HashMap());
    }
}
