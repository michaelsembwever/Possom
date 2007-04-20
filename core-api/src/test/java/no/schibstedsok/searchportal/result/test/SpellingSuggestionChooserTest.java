// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooser;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelTestCase;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooserResultHandlerConfig;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.view.config.SearchTab;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;



/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class SpellingSuggestionChooserTest extends DataModelTestCase {

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testNoSuggestions()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermOneSuggestion()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand("slankting"));
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermOneSuggestionwithLimit()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result);
        assertEquals(0, numberOfTermsCorrected(result));
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermTwoSuggestionsSameScore()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
        final BasicSearchResult result = new BasicSearchResult(new MockupSearchCommand());
        final SpellingSuggestion suggestion = new SpellingSuggestion("slankting", "slakting", 227);
        final SpellingSuggestion suggestion2 = new SpellingSuggestion("slankting", "slanking", 227);
        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(2, numberOfSuggestions(result, "slankting"));
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermNumberOfSuggestionsSameScoreOnLimit()  throws Exception{
        
        final SpellingSuggestionChooserResultHandlerConfig config = new SpellingSuggestionChooserResultHandlerConfig();
        config.setMinScore(-1);
        config.setMaxDistance(3);
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(config);
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermNumberOfSuggestionsSameScoreOvertLimit()  throws Exception{
        
        final SpellingSuggestionChooserResultHandlerConfig config = new SpellingSuggestionChooserResultHandlerConfig();
        config.setMinScore(-1);
        config.setMaxDistance(3);
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(config);
        
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermNumberOfSuggestionsDiffScoreOvertLimit()  throws Exception{

        final SpellingSuggestionChooserResultHandlerConfig config = new SpellingSuggestionChooserResultHandlerConfig();
        config.setMinScore(-1);
        config.setMaxDistance(3);
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(config);
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermNumberOfSuggestionsSameScoreLimitToZero()  throws Exception{
        
        final SpellingSuggestionChooserResultHandlerConfig config = new SpellingSuggestionChooserResultHandlerConfig();
        config.setMinScore(-1);
        config.setMaxDistance(0);
        final SpellingSuggestionChooser chooser = new SpellingSuggestionChooser(config);        
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneTermTwoSuggestionsDifferentScore()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testTwoTermsBothWrong()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testTwoTermsBothWrongManySuggestions()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testTwoTermsBothWrongManySuggestionsOneMuchBetter()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testThreeTermsTwoWrong()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testThreeTermsOneWrong()  throws Exception{
        
        final SpellingSuggestionChooser chooser 
                = new SpellingSuggestionChooser(new SpellingSuggestionChooserResultHandlerConfig());
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

    private void handleResult(
            final SpellingSuggestionChooser chooser, 
            final SearchResult result) throws SiteKeyedFactoryInstantiationException {
        
        final DataModel datamodel = getDataModel();
        
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
                return datamodel.getQuery().getString();
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
        chooser.handleResult(resultHandlerContext, datamodel);
    }
}
