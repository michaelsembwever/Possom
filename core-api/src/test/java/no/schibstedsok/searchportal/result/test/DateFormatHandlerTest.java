// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.handler.DateFormatHandler;
import no.schibstedsok.searchportal.result.handler.DateFormatResultHandlerConfig;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.*;
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

    /**
     * 
     * @param testName 
     */
    public DateFormatHandlerTest(String testName) {
        super(testName);
    }

    private ResultHandler.Context getResultHandlerContext() throws SiteKeyedFactoryInstantiationException{

        final MockupSearchCommand command = new MockupSearchCommand();
        final BasicSearchResult<ResultItem> bsr = new BasicSearchResult<ResultItem>();
        final ResultHandler.Context cxt = new ResultHandler.Context() {

            public ResultList<ResultItem> getSearchResult() {
                return bsr;
            }
            
            public SearchConfiguration getSearchConfiguration(){
                return command.getSearchConfiguration();
            }

            public SearchTab getSearchTab() {
                return command.getRunningQuery().getSearchTab();
            }

            public String getQueryString() {
                return command.getRunningQuery().getQuery().getQueryString();
            }

            public Query getQuery() {
                return command.getRunningQuery().getQuery();
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
            public BytecodeLoader newBytecodeLoader(final SiteContext context, final String className) {
                return FileResourceLoader.newBytecodeLoader(context, className);
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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneWithoutPrefix() throws Exception{
        
        final DateFormatResultHandlerConfig config = new DateFormatResultHandlerConfig();
        config.setSourceField(SOURCE_FIELD);
        final DateFormatHandler rh = new DateFormatHandler(config);

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

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testOneWithPrefix() throws Exception{

        final ResultHandler.Context resultHandlerContext = getResultHandlerContext();
        
        final DateFormatResultHandlerConfig config = new DateFormatResultHandlerConfig();
        config.setSourceField(SOURCE_FIELD);
        config.setFieldPrefix(FIELD_PREFIX);
        final DateFormatHandler rh = new DateFormatHandler(config);        

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
