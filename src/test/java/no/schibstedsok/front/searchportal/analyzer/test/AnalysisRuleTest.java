/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.analyzer.test;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import no.schibstedsok.front.searchportal.analyzer.AnalysisRule;
import junit.framework.TestCase;

/**
 * A test class for {@link no.schibstedsok.front.searchportal.analyzer.AnalysisRule}.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class AnalysisRuleTest extends TestCase {

    private AnalysisRule rule = null;
    private Predicate truePredicate = PredicateUtils.truePredicate();
    private Predicate falsePredicate = PredicateUtils.falsePredicate();

    protected void setUp() throws Exception {
        super.setUp();
        this.rule = new AnalysisRule();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for 'no.schibstedsok.front.searchportal.analyzer.AnalysisRule.addPredicateScore(Predicate, int)'.
     */
    public void testAddPredicateScore() {
        rule.addPredicateScore(truePredicate, 0);
    }

    /**
     * Test method for 'no.schibstedsok.front.searchportal.analyzer.AnalysisRule.evaluate(String, TokenEvaluatorFactory)'.
     */
    public void testEvaluate() {
        // Empty rule should eval to zero.
        int score = rule.evaluate("", null);
        assertEquals(0, score);

        // One true predicate.
        rule.addPredicateScore(truePredicate, 10);
        score = rule.evaluate("", null);
        assertEquals(10 , score);

        // Two true predicates. Contributes to the score.
        rule.addPredicateScore(truePredicate, -5);
        score = rule.evaluate("", null);
        assertEquals(5, score);
        
        // False predicate. Does not contribute to the score.
        rule.addPredicateScore(falsePredicate, -200);
        score = rule.evaluate("", null);
        assertEquals(5, score);
    }
}
