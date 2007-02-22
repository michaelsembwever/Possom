// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.handler.DateFormatHandler;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.view.config.SearchTab;
import org.apache.log4j.Logger;

import static no.schibstedsok.searchportal.result.handler.DateFormatHandler.Fields;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/** Fast navigation tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class DateFormatHandlerTest extends SiteTestCase {

    private static final Logger LOG = Logger.getLogger(DateFormatHandlerTest.class);

    private static final String SOURCE_FIELD = "source_field";
    private static final String FIELD_PREFIX = "prefix";
    
    private final DateFormatHandler rh;

    public DateFormatHandlerTest(String testName) {
        super(testName);
        
        rh = new DateFormatHandler();
        rh.setSourceField(SOURCE_FIELD);
    }	
    
    private ResultHandler.Context getResultHandlerContext(){
        
        final MockupSearchCommand command = new MockupSearchCommand();
        final BasicSearchResult bsr = new BasicSearchResult(command);
        final ResultHandler.Context cxt = new ResultHandler.Context() {

            public SearchResult getSearchResult() {
                return bsr;
            }

            public SearchTab getSearchTab() {
                return null;
            }

            public String getQueryString() {
                return null;
            }

            public Query getQuery() {
                return null;
            }

            public void addSource(final Modifier modifier) {
                command.getRunningQuery().addSource(modifier);
            }

            public Site getSite() {
                return getTestingSite();
            }

            public DocumentLoader newDocumentLoader(final SiteContext siteCxt,
                                                    final String resource,
                                                    final DocumentBuilder builder) {
                
                return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }

            public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                        final String resource,
                                                        final Properties properties) {
                
                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
        };
        cxt.getSearchResult().addResult(createItem("2006-04-27T10:11:12Z"));
        return cxt;
    }

    private BasicSearchResultItem createItem(String time) {
        
        final BasicSearchResultItem bsri = new BasicSearchResultItem();
        bsri.addField(SOURCE_FIELD, time);
        return bsri;
    }
    
    @Test
    public void testOneWithoutPrefix() {
        
        final ResultHandler.Context resultHandlerContext = getResultHandlerContext();
        
        rh.handleResult(resultHandlerContext, null);
        
        assertEquals(1, resultHandlerContext.getSearchResult().getResults().size());
        BasicSearchResultItem bsri = (BasicSearchResultItem) resultHandlerContext.getSearchResult().getResults().get(0);
        
        assertEquals("2006", bsri.getField(Fields.YEAR.name()));
        assertEquals("04", bsri.getField(Fields.MONTH.name()));
        assertEquals("27", bsri.getField(Fields.DAY.name()));
        assertEquals("10", bsri.getField(Fields.HOUR.name()));
        assertEquals("11", bsri.getField(Fields.MINUTE.name()));
        assertEquals("12", bsri.getField(Fields.SECOND.name()));
    }

    @Test
    public void testOneWithPrefix() {
        
        final ResultHandler.Context resultHandlerContext = getResultHandlerContext();
        
        rh.setFieldPrefix(FIELD_PREFIX);
        rh.handleResult(resultHandlerContext, null);
        
        assertEquals(1, resultHandlerContext.getSearchResult().getResults().size());
        BasicSearchResultItem bsri = (BasicSearchResultItem) resultHandlerContext.getSearchResult().getResults().get(0);
        assertEquals("2006", bsri.getField(FIELD_PREFIX + Fields.YEAR.name()));
        assertEquals("04", bsri.getField(FIELD_PREFIX + Fields.MONTH.name()));
        assertEquals("27", bsri.getField(FIELD_PREFIX + Fields.DAY.name()));
        assertEquals("10", bsri.getField(FIELD_PREFIX + Fields.HOUR.name()));
        assertEquals("11", bsri.getField(FIELD_PREFIX + Fields.MINUTE.name()));
        assertEquals("12", bsri.getField(FIELD_PREFIX + Fields.SECOND.name()));
    }
}
