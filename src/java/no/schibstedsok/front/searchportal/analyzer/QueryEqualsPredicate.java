/*
 * Copyright (2005) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;

public class QueryEqualsPredicate implements Predicate {

    private String s;
    
    public QueryEqualsPredicate(String s) {
        this.s = s;
    }

    public boolean evaluate(Object evalFactory) {
        TokenEvaluatorFactory tokenEvaluatorFactory = (TokenEvaluatorFactory) evalFactory;
        if (tokenEvaluatorFactory.getQueryString() == null) {
            return false;
        } else {
            return tokenEvaluatorFactory.getQueryString().equals(s);
        }
    }
}
