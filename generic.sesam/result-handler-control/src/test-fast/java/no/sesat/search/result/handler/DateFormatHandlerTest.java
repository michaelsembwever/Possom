/* Copyright (2006-2007) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result.handler;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelTestCase;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.query.Query;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.test.MockupSearchCommand;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.*;
import no.sesat.search.view.config.SearchTab;
import org.apache.log4j.Logger;
import static no.sesat.search.result.handler.DateFormatHandler.Fields;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/** Fast navigation tests.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class DateFormatHandlerTest extends DataModelTestCase {

    private static final Logger LOG = Logger.getLogger(DateFormatHandlerTest.class);

    private static final String SOURCE_FIELD = "source_field";
    private static final String FIELD_PREFIX = "prefix";

    private ResultHandler.Context getResultHandlerContext() throws SiteKeyedFactoryInstantiationException{

        final DataModel datamodel = getDataModel();

        final MockupSearchCommand command = new MockupSearchCommand();
        final BasicResultList<ResultItem> bsr = new BasicResultList<ResultItem>();

        final ResultHandler.Context cxt = new ResultHandler.Context() {
            public ResultList<ResultItem> getSearchResult() {
                return bsr;
            }
            public SearchConfiguration getSearchConfiguration(){
                return command.getSearchConfiguration();
            }
            public SearchTab getSearchTab() {
                return datamodel.getPage().getCurrentTab();
            }
            public String getDisplayQuery() {
                return datamodel.getQuery().getString();
            }
            public Query getQuery() {
                return datamodel.getQuery().getQuery();
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
            public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                return FileResourceLoader.newBytecodeLoader(site, name, jar);
            }
        };
        cxt.getSearchResult().addResult(createItem("2006-04-27T10:11:12Z"));
        return cxt;
    }

    private BasicResultItem createItem(String time) {

        final BasicResultItem bsri = new BasicResultItem();
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
        BasicResultItem bsri = (BasicResultItem) resultHandlerContext.getSearchResult().getResults().get(0);

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
        BasicResultItem bsri = (BasicResultItem) resultHandlerContext.getSearchResult().getResults().get(0);
        assertEquals("2006", bsri.getField(FIELD_PREFIX + Fields.YEAR.name()));
        assertEquals("04", bsri.getField(FIELD_PREFIX + Fields.MONTH.name()));
        assertEquals("27", bsri.getField(FIELD_PREFIX + Fields.DAY.name()));
        assertEquals("10", bsri.getField(FIELD_PREFIX + Fields.HOUR.name()));
        assertEquals("11", bsri.getField(FIELD_PREFIX + Fields.MINUTE.name()));
        assertEquals("12", bsri.getField(FIELD_PREFIX + Fields.SECOND.name()));
    }
}
