/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.analyser;

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
     * Test method for 'no.sesat.search.analyzer.AnalysisRule.addPredicateScore(Predicate, int)'.
     */
    @Test
    public void testAddPredicateScore() {
        rule.addPredicateScore(truePredicate, 0);
    }

}
