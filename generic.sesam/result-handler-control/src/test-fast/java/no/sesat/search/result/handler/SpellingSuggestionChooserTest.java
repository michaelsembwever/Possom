/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result.handler;

import no.sesat.search.result.BasicWeightedSuggestion;
import no.sesat.search.query.Query;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelTestCase;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.WeightedSuggestion;
import no.sesat.search.result.test.MockupSearchCommand;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.view.config.SearchTab;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;



/**
 *
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
