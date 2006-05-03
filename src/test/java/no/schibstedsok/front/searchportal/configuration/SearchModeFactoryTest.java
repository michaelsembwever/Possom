// Copyright (2006) Schibsted Søk AS
/*
 * SearchModeFactoryTest.java
 * JUnit based test
 *
 * Created on April 19, 2006, 3:31 PM
 */

package no.schibstedsok.front.searchportal.configuration;

import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class SearchModeFactoryTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(SearchModeFactoryTest.class);

    public SearchModeFactoryTest(final String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getModeFactory method, of class no.schibstedsok.front.searchportal.configuration.SearchModeFactory.
     */
    public void testGetModeFactory(){

        assertNotNull(getModeFactory());
    }

    private SearchModeFactory getModeFactory() {
        LOG.trace("getModeFactory");

        final SearchModeFactory.Context cxt = new SearchModeFactory.Context(){
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite()  {
                return Site.DEFAULT;
            }
        };

        final SearchModeFactory result = SearchModeFactory.getModeFactory(cxt);
        assertNotNull(result);

        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");

        return result;
    }

    /**
     * Test of getMode method, of class no.schibstedsok.front.searchportal.configuration.SearchModeFactory.
     */
    public void testGetMode() {
        LOG.trace("testGetMode");

        final String id = "norsk-magic";
        final SearchModeFactory instance = getModeFactory();

        final SearchMode result = instance.getMode(id);
        assertNotNull(result);
    }

}