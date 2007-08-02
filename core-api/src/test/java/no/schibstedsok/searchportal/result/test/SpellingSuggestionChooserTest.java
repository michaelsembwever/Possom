// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import no.schibstedsok.searchportal.result.BasicWeightedSuggestion;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooser;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelTestCase;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.WeightedSuggestion;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooserResultHandlerConfig;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.BytecodeLoader;
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

        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand("slankting");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("slankting", "slankinga", "slankinga",227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("slankting", "slankinga", "slankinga", 227);

        final BasicWeightedSuggestion suggestion4
                = BasicWeightedSuggestion.instanceOf("slankting", "slafnkinga", "slafnkinga", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 211);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("slankting", "slankinga", "slankinga", 223);

        final BasicWeightedSuggestion suggestion4
                = BasicWeightedSuggestion.instanceOf("slankting", "slafnkinga", "slafnkinga", 227);

        final BasicWeightedSuggestion suggestion5
                = BasicWeightedSuggestion.instanceOf("slankting", "slankinga", "slankinga", 227);

        final BasicWeightedSuggestion suggestion6
                = BasicWeightedSuggestion.instanceOf("slankting", "slafnkinga", "slafnkinga", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        result.addSpellingSuggestion(suggestion5);
        result.addSpellingSuggestion(suggestion6);
        handleResult(chooser, result, sc);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(3, numberOfSuggestions(result, "slankting"));

        final List suggestionList = (List) result.getSpellingSuggestionsMap().get("slankting");

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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("slankting", "slankinga", "slankinga", 227);

        final BasicWeightedSuggestion suggestion4
                = BasicWeightedSuggestion.instanceOf("slankting", "slafnkinga", "slafnkinga", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        result.addSpellingSuggestion(suggestion4);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand();
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("slankting", "slanking", "slanking", 230);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result, sc);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, numberOfSuggestions(result, "slankting"));
        final List suggestionList = (List) result.getSpellingSuggestionsMap().get("slankting");
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
        final SearchCommand sc = new MockupSearchCommand("slankting sykel");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkel", "sykkel", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        handleResult(chooser, result, sc);
        assertEquals(2, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestionsMap().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestionsMap().get("sykel");
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
        final SearchCommand sc = new MockupSearchCommand("slankting sykel");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkel", "sykkel", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkkel", "sykkkel", 227);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result, sc);
        assertEquals(1, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestionsMap().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestionsMap().get("sykel");
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
        final SearchCommand sc = new MockupSearchCommand("slankting sykel");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkel", "sykkel", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkkel", "sykkkel", 240);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result, sc);
        assertEquals(2, numberOfTermsCorrected(result));
        final List suggestionList = (List) result.getSpellingSuggestionsMap().get("slankting");
        assertTrue(suggestionList.contains(suggestion));
        final List suggestionList2 = (List) result.getSpellingSuggestionsMap().get("sykel");
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
        final SearchCommand sc = new MockupSearchCommand("slankting sykel bil");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        final BasicWeightedSuggestion suggestion2
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkel", "sykkel", 227);

        final BasicWeightedSuggestion suggestion3
                = BasicWeightedSuggestion.instanceOf("sykel", "sykkkel", "sykkkel", 240);

        result.addSpellingSuggestion(suggestion);
        result.addSpellingSuggestion(suggestion2);
        result.addSpellingSuggestion(suggestion3);
        handleResult(chooser, result, sc);
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
        final SearchCommand sc = new MockupSearchCommand("slankting sykkel bil");
        final BasicResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BasicWeightedSuggestion suggestion
                = BasicWeightedSuggestion.instanceOf("slankting", "slakting", "slakting", 227);

        result.addSpellingSuggestion(suggestion);
        handleResult(chooser, result, sc);
        assertEquals(1, numberOfTermsCorrected(result));
        assertEquals(1, result.getQuerySuggestions().size());
    }


    private int numberOfTermsCorrected(final BasicResultList<ResultItem> result) {

        return result.getSpellingSuggestionsMap().keySet().size();
    }

    private int numberOfSuggestions(final BasicResultList<ResultItem> result, final String term) {

        final List<WeightedSuggestion> listOfSuggestions
                = (List<WeightedSuggestion>) result.getSpellingSuggestionsMap().get(term);

        return listOfSuggestions.size();
    }

    private void handleResult(
            final SpellingSuggestionChooser chooser,
            final ResultList<ResultItem> result,
            final SearchCommand command) throws SiteKeyedFactoryInstantiationException {

        final DataModel datamodel = getDataModel();

        final ResultHandler.Context resultHandlerContext = new ResultHandler.Context() {
            public ResultList<ResultItem> getSearchResult() {
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
            public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                return FileResourceLoader.newBytecodeLoader(site, name, jar);
            }
            public String getDisplayQuery() {
                return datamodel.getQuery().getString();
            }
            public Query getQuery() {
                return datamodel.getQuery().getQuery();
            }
            public SearchTab getSearchTab(){
                return datamodel.getPage().getCurrentTab();
            }
            public SearchConfiguration getSearchConfiguration(){
                return command.getSearchConfiguration();
            }

        };
        chooser.handleResult(resultHandlerContext, datamodel);
    }
}
