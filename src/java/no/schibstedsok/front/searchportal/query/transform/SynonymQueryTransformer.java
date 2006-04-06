/*
 * SynonymQueryTransformer.java
 *
 * Created on April 5, 2006, 8:05 PM
 *
 */

package no.schibstedsok.front.searchportal.query.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

/**
 *
 * @author maek
 */
public final class SynonymQueryTransformer extends AbstractQueryTransformer {
    
    /** Synonym expansion are only performed for clauses matching the predicates
     * contained in predicateNames */
    private final Collection<String> predicateNames = new ArrayList<String>();
    private Collection<TokenPredicate> predicates = null;
    
    private final List<LeafClause> leafs = new ArrayList<LeafClause>();
    private final Set<TokenPredicate> matchingPredicates = new HashSet<TokenPredicate>();
    private final List<LeafClause> expanded = new ArrayList<LeafClause>();
    
    private final StringBuilder builder = new StringBuilder();

    private boolean fromDefault = false;
    
    public void addPredicateName(final String name) {
        predicateNames.add(name);
    }
    
    protected void visitImpl(final DefaultOperatorClause clause) {
        for (final TokenPredicate p : getPredicates()) {
            
            if (clause.getKnownPredicates().contains(p)
            || clause.getPossiblePredicates().contains(p)) {
                matchingPredicates.add(p);
            }
        }
        
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }
    
    protected void visitImpl(final LeafClause clause) {
        if (! matchingPredicates.isEmpty() && !expanded.contains(clause)) {
            for (final TokenPredicate p : matchingPredicates) {
                
                if (matchingPredicates.size() > 0) {
                    builder.append(' ');
                }
                
                if (isSynonym(builder.toString() + clause.getTerm())) {
                    builder.append(clause.getTerm());
                    leafs.add(clause);
                } 
                else {
                    if (!leafs.isEmpty()) {
                        expandSynonym(leafs, getSynonym(builder.toString()));
                        expanded.addAll(leafs);
                        leafs.clear();
                        matchingPredicates.clear();
                        builder.setLength(0);
                    }
                }
            }
        }
        
        if (clause == getContext().getQuery().getFirstLeafClause()) {
            for (TokenPredicate predicate : getPredicates()) {
                if (clause.getPossiblePredicates().contains(predicate)
                || clause.getKnownPredicates().contains(predicate)) {
                    if (isSynonym(getContext().getTransformedTerms().get(clause))) {
                        expandSynonym(clause, getSynonym(getContext().getTransformedTerms().get(clause)));
                        expanded.add(clause);
                        return;
                    }
                }
            }
        }
    }
    
    private void expandSynonym(final List<LeafClause> replace, String synonym) {
        final LeafClause first = replace.get(0);
        final LeafClause last = replace.get(replace.size() - 1);
        
        if (first != last) {
            getContext().getTransformedTerms().put(first, "(" + first.getTerm());
            getContext().getTransformedTerms().put(last, last.getTerm()+ " " + synonym + ")");
        } else {
            getContext().getTransformedTerms().put(last, "(" + last.getTerm()+ " " + synonym + ")");
        }
    }
    
    private void expandSynonym(final LeafClause replace, String synonym) {
        final String originalTerm = getContext().getTransformedTerms().get(replace);
        getContext().getTransformedTerms().put(replace, "(" + originalTerm + " " + synonym + ")");
    }
    
    private Collection<TokenPredicate> getPredicates() {
        synchronized (this) {
            if (predicates == null) {
                predicates = new ArrayList<TokenPredicate>();
                for (final String predicateName : predicateNames) {
                    final TokenPredicate p = TokenPredicate.valueOf(predicateName);
                    predicates.add(p);
                }
            }
        }
        return predicates;
    }
    
    private boolean isSynonym(String string) {
        return string.trim().equalsIgnoreCase("sch") || string.trim().equalsIgnoreCase("schibsted") || string.trim().equalsIgnoreCase("schibsted asa");
    }
    
    private String getSynonym(String string) {
        if (string.trim().equalsIgnoreCase("sch")) {
            return "schibsted";
        }
        
        if (string.trim().equalsIgnoreCase("schibsted")) {
            return "sch";
        }
        
        if (string.trim().equalsIgnoreCase("schibsted asa")) {
            return "schasa";
        }
        
        return null;
    }
}
