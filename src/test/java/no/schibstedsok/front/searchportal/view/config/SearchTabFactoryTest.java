// Copyright (2006) Schibsted Søk AS
/*
 * SearchTabFactoryTest.java
 * JUnit based test
 *
 * Created on 20 April 2006, 11:30
 */

package no.schibstedsok.front.searchportal.view.config;

import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class SearchTabFactoryTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(SearchTabFactoryTest.class);

    public SearchTabFactoryTest(final String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getViewFactory method, of class no.schibstedsok.front.searchportal.view.config.SearchTabFactory.
     */
    public void testGetViewFactory() {

        assertNotNull(getViewFactory());
    }

    /**
     * Test of testGetTabByName method, of class no.schibstedsok.front.searchportal.view.config.SearchTabFactory.
     */
    public void testGetTabByName() {

        LOG.trace("testGetTab");

        final String id = "norwegian-internet";
        final SearchTabFactory instance = getViewFactory();

        final SearchTab result = instance.getTabByName(id);
        assertNotNull(result);
    }

    /**
     * Test of testGetTabByKey method, of class no.schibstedsok.front.searchportal.view.config.SearchTabFactory.
     */
    public void testGetTabByKey() {

        LOG.trace("testGetTab");

        final String key = "d";
        final SearchTabFactory instance = getViewFactory();

        final SearchTab result = instance.getTabByKey(key);
        assertNotNull(result);
    }


    private SearchTabFactory getViewFactory() {

        LOG.trace("getModeFactory");

        final SearchTabFactory.Context cxt = new SearchTabFactory.Context(){
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite()  {
                return Site.DEFAULT;
            }
        };

        final SearchTabFactory result = SearchTabFactory.getTabFactory(cxt);
        assertNotNull(result);

        return result;
    }

}
