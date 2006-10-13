/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.analyser;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import no.schibstedsok.searchportal.TestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A test class for {@link no.schibstedsok.searchportal.analyzer.AnalysisRule}.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class AnalysisRuleTest extends junit.framework.TestCase {

    private AnalysisRule rule = null;
    private Predicate truePredicate = PredicateUtils.truePredicate();
    private Predicate falsePredicate = PredicateUtils.falsePredicate();

    public AnalysisRuleTest(String testName) {
        super(testName);
    }	 
    
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        this.rule = new AnalysisRule();
    }

    /**
     * Test method for 'no.schibstedsok.searchportal.analyzer.AnalysisRule.addPredicateScore(Predicate, int)'.
     */
    @Test
    public void testAddPredicateScore() {
        rule.addPredicateScore(truePredicate, 0);
    }

}
