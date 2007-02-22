/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.analyser;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A test class for {@link AnalysisRule}.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class AnalysisRuleTest  {

    private AnalysisRule rule = null;
    private Predicate truePredicate = PredicateUtils.truePredicate();
    private Predicate falsePredicate = PredicateUtils.falsePredicate();


    @BeforeClass
    protected void setUp() throws Exception {
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
