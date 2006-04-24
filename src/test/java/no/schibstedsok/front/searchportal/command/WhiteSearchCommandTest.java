/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.query.run.RunningQueryImpl;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import no.schibstedsok.front.searchportal.view.config.SearchTabFactory;


/**
 *
 * @author magnuse
 */
public class WhiteSearchCommandTest extends TestCase {
    
    public WhiteSearchCommandTest(String name) {
        super(name);
    }
    
    public void testQueryRepresentationInteger() {
        final String query = getParsedQueryAsString("524287");
        assertEquals("whitepages:524287", query);
    }

    public void testQueryRepresentationWord() {
        final String query = getParsedQueryAsString("word");
        assertEquals("whitephon:word", query);
    }

    public void testQueryRepresentationTwoWords() {
        final String query = getParsedQueryAsString("word word2");
        assertEquals("whitephon:word whitephon:word2", query);
    }

    public void testQueryRepresentationPhoneNumber() {
        final String query = getParsedQueryAsString("97403306");
        assertEquals("whitepages:97403306", query);

        final String queryWithSpaces = getParsedQueryAsString("97 40 33 06");
        assertEquals("whitepages:97403306", query);
    }

    public void testQueryRepresentationPhrase() {
        final String query = getParsedQueryAsString("\"magnus eklund\"");
        assertEquals("whitephon:magnus whitephon:eklund", query);
    }

    public void testQueryRepresentationMixed() {
        final String query = getParsedQueryAsString("\"magnus eklund\" 97 40 3306 oslo sarsgate 74");
        assertEquals("whitephon:magnus whitephon:eklund whitepages:97403306 whitephon:oslo whitephon:sarsgate whitepages:74", query);
    }
    
    public void testIgnoreField() {
        final String query = getParsedQueryAsString("site:vg.no magnus eklund");
        assertEquals("whitephon:magnus whitephon:eklund", query.trim());
    }
    
    private String getParsedQueryAsString(final String query) {
        final SearchCommand.Context cxt = createCommandContext(query);
        final WhiteSearchCommand command = createSearchCommand(cxt);
        return command.getQueryRepresentation(cxt.getQuery());
       
    }
    
    private WhiteSearchCommand createSearchCommand(final SearchCommand.Context cxt) {
        return new WhiteSearchCommand(cxt, Collections.EMPTY_MAP);
    }
    
    private SearchCommand.Context createCommandContext(final String query) {
        final FastConfiguration config = new FastConfiguration();
        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            private final SearchMode mode = new SearchMode();
            
            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.getTabFactory(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this))
                    .getTabByKey("w");
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            
            public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            
            public Site getSite() {
                return Site.DEFAULT;
            }
            
            
        };
        
        final RunningQuery rq = new RunningQueryImpl(rqCxt, query, new HashMap());
        
        final SearchCommand.Context searchCmdCxt = new SearchCommand.Context() {
            public SearchConfiguration getSearchConfiguration() {
                return config;
            }
            
            public RunningQuery getRunningQuery() {
                return rq;
            }
            
            public Site getSite() {
                return Site.DEFAULT;
            }
            
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            
            public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            
            public Query getQuery(){
                return rq.getQuery();
            }
        };
        
        return searchCmdCxt;
    }
}
