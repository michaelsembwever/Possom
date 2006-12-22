/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.util.test;

import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.util.PagingDisplayHelper;
import junit.framework.TestResult;

/** Test a PagingDisplayHelper.
 * 
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PagingDisplayHelperUnitTest extends SiteTestCase {

    private PagingDisplayHelper pager;
    
    public PagingDisplayHelperUnitTest(final String testName) {
        super(testName);
    }	     

    protected void setUp() throws Exception {
        super.setUp();

        pager = new PagingDisplayHelper(10, 10);
    }

    public void testPageSets() {
        pager.setNumberOfResults(100);

        assertEquals(1, pager.getFirstVisiblePage());
        assertEquals(10, pager.getLastVisiblePage());

        pager.setCurrentOffset(9);
        assertEquals(1, pager.getFirstVisiblePage());
        assertEquals(10, pager.getLastVisiblePage());

        pager.setNumberOfResults(200);
        pager.setCurrentOffset(0);

        assertEquals(20, pager.getNumberOfPages());

        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());
        pager.setCurrentOffset(pager.getOffsetOfNextPage());

        assertEquals(6, pager.getFirstVisiblePage());
        assertEquals(15, pager.getLastVisiblePage());

        pager.setNumberOfResults(93);
        pager.setCurrentOffset(0);

        assertEquals(10, pager.getOffsetOfNextPage());
        assertEquals(10, pager.getOffsetOfPage(2));

        assertEquals(10, pager.getNumberOfPages());
        assertEquals(10, pager.getLastVisiblePage());
    }

    public void testPaging() {
        pager.setNumberOfResults(100);

        // Default offset is 0
        assertEquals(1, pager.getCurrentPage());
        assertEquals(1, pager.getFirstHitOnPage());
        assertEquals(10, pager.getLastHitOnPage());
        assertEquals(10, pager.getNumberOfPages());
        assertEquals(10, pager.getOffsetOfNextPage());
        assertTrue(pager.isFirstPage());

        pager.setCurrentOffset(1);

        assertEquals(2, pager.getFirstHitOnPage());
        assertEquals(11, pager.getLastHitOnPage());
        assertEquals(10, pager.getNumberOfPages());
        assertEquals(11, pager.getOffsetOfNextPage());

        pager.setCurrentOffset(9);

        assertEquals(10, pager.getFirstHitOnPage());
        assertEquals(19, pager.getLastHitOnPage());
        assertEquals(10, pager.getNumberOfPages());
        assertEquals(19, pager.getOffsetOfNextPage());

        pager.setCurrentOffset(0);

        assertEquals(10, pager.getOffsetOfNextPage());

        pager.setCurrentOffset(pager.getOffsetOfNextPage());

        assertEquals(2, pager.getCurrentPage());
        assertEquals(11, pager.getFirstHitOnPage());
        assertEquals(20, pager.getLastHitOnPage());

        pager.setCurrentOffset(99);
        assertTrue(pager.isLastPage());
        assertFalse(pager.isFirstPage());
        assertEquals(10, pager.getCurrentPage());

        pager.setCurrentOffset(pager.getOffsetOfPreviousPage());
        assertFalse(pager.isLastPage());

        pager.setNumberOfResults(200);
        pager.setCurrentOffset(0);

        assertEquals(20, pager.getNumberOfPages());
    }

    public void testDifferentPageSize() {

        pager = new PagingDisplayHelper(12, 10);
        pager.setNumberOfResults(2000);
        pager.setCurrentOffset(120);

        assertEquals(6, pager.getFirstVisiblePage());

        pager.setCurrentOffset(0);
        assertEquals(1, pager.getFirstVisiblePage());

        pager.setCurrentOffset(108);
        assertEquals(5, pager.getFirstVisiblePage());

        pager.setCurrentOffset(30);
        assertEquals(1, pager.getFirstVisiblePage());

        pager.setCurrentOffset(240);
        assertEquals(16, pager.getFirstVisiblePage());
    }

    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void run(final TestResult testResult) {
        super.run(testResult);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public TestResult run() {
        return super.run();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
