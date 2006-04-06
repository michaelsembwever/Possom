/*
 * SynonymQueryTransformer.java
 *
 * Created on April 5, 2006, 8:05 PM
 *
 */

package no.schibstedsok.front.searchportal.query.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/**
 *
 * @author maek
 */
public final class SynonymQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(SynonymQueryTransformer.class);
    private static final String ERR_FAILED_LOADING_TICKER_MAP = "Failed to load tickers";

    private static final Map<String,String> SYNONYMS;
    private static final Map<String,String> REVERSE_SYNONYMS;

    static{

        final Map<String,String> synonyms = new HashMap<String,String>();
        final Map<String,String> reverseSynonyms = new HashMap<String,String>();

        try{
            final Properties props = new Properties();
            props.load(SynonymQueryTransformer.class.getResourceAsStream("/tickers.properties"));

            for( Map.Entry<Object,Object> entry : props.entrySet()){

                final String key = ((String)entry.getKey()).toLowerCase();
                final String value = ((String)entry.getValue()).toLowerCase();
                synonyms.put( key, value );
                reverseSynonyms.put( value, key );
            }

        }catch(IOException ioe){
            LOG.info(ERR_FAILED_LOADING_TICKER_MAP, ioe);
        }finally{

            SYNONYMS = Collections.unmodifiableMap(synonyms);
            REVERSE_SYNONYMS = Collections.unmodifiableMap(reverseSynonyms);
        }
    }
    
    /** Synonym expansion are only performed for clauses matching the predicates
     * contained in predicateNames */
    private Collection<String> predicateNames = new ArrayList<String>();
    private Collection<TokenPredicate> predicates = null;
    
    private List<LeafClause> leafs = new ArrayList<LeafClause>();
    private Set<TokenPredicate> matchingPredicates = new HashSet<TokenPredicate>();
    private List<LeafClause> expanded = new ArrayList<LeafClause>();
    
    private StringBuilder builder = new StringBuilder();
    
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
                } else {
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
                    predicates.add(TokenPredicate.valueOf(predicateName));
                }
            }
        }
        return predicates;
    }
    
    private boolean isSynonym(String string) {

        final String s = string.toLowerCase();
        return SYNONYMS.containsKey(s) || REVERSE_SYNONYMS.containsKey(s);
    }
    
    private String getSynonym(final String string) {

        final String s = string.toLowerCase();
        return SYNONYMS.containsKey(s)
                ? SYNONYMS.get(s)
                : REVERSE_SYNONYMS.get(s);
    }
    
    public Object clone() throws CloneNotSupportedException {
        final SynonymQueryTransformer retValue = (SynonymQueryTransformer)super.clone();
        
        retValue.predicateNames = predicateNames;
        retValue.matchingPredicates = new HashSet<TokenPredicate>();
        retValue.builder = new StringBuilder();
        retValue.leafs = new ArrayList<LeafClause>();
        
        return retValue;
    }

    public StringBuilder getBuilder() {
        return builder;
    }
}
