// Copyright (2006) Schibsted Søk AS
/*
 * SynonymQueryTransformer.java
 *
 * Created on April 5, 2006, 8:05 PM
 *
 */

package no.schibstedsok.searchportal.query.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;
/** XXX This will get largely rewritten when alternation rotation comes into play.
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

            for(Map.Entry<Object,Object> entry : props.entrySet()){

                final String key = ((String)entry.getKey()).toLowerCase();
                final String value = ((String)entry.getValue()).toLowerCase();
                synonyms.put(key, value);
                reverseSynonyms.put(value, key);
            }

        }catch(IOException ioe){
            LOG.info(ERR_FAILED_LOADING_TICKER_MAP, ioe);
        }finally{

            SYNONYMS = Collections.unmodifiableMap(synonyms);
            REVERSE_SYNONYMS = Collections.unmodifiableMap(reverseSynonyms);
        }
    }

    private static final Collection<TokenPredicate> DEFAULT_PREFIXES = Collections.unmodifiableCollection(
            Arrays.asList(
                TokenPredicate.STOCKMARKETFIRMS,
                TokenPredicate.STOCKMARKETTICKERS
            ));

    /** Synonym expansion are only performed for clauses matching the predicates
     * contained in predicateNames */
    private Collection<String> predicateNames = new ArrayList<String>();
    private Collection<TokenPredicate> customPredicates;

    private List<LeafClause> leafs = new ArrayList<LeafClause>();
    private Set<TokenPredicate> matchingPredicates = new HashSet<TokenPredicate>();
    private List<LeafClause> expanded = new ArrayList<LeafClause>();

    private StringBuilder builder = new StringBuilder();

    /** TODO comment me. **/
    void addPredicateName(final String name) {
        predicateNames.add(name);
    }

    /** TODO comment me. **/
    protected void visitImpl(final DoubleOperatorClause clause) {

        LOG.trace("visitImpl(" + clause + ')');

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);

    }

    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {

        LOG.trace("visitImpl(" + clause + ')');

        for (final TokenPredicate p : getPredicates()) {

                final Query query = getContext().getQuery();
                final List<OperationClause> parents
                        = query.getParentFinder().getParents(query.getRootClause(), clause);

                for(OperationClause oc : parents){
                    if(oc.getKnownPredicates().contains(p) || oc.getPossiblePredicates().contains(p)){
                        LOG.debug("adding to matchingPredicates " + p);
                        matchingPredicates.add(p);
                    }
                }
        }

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);

    }

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {

        LOG.trace("visitImpl(" + clause + ')');

        if (!matchingPredicates.isEmpty() && !expanded.contains(clause)) {
            for (final TokenPredicate p : matchingPredicates) {

                if (isSynonym(builder.toString() + clause.getTerm())) {

                    LOG.debug("adding to builder " + clause.getTerm());
                    builder.append(clause.getTerm());
                    leafs.add(clause);
                } //else {
//                    if (!leafs.isEmpty()) {
//
//                        expandSynonym(leafs, getSynonym(builder.toString()));
//                        expanded.addAll(leafs);
//                        leafs.clear();
//                        matchingPredicates.clear();
//                        builder.setLength(0);
//                    }
//                }
            }
        }

        for (TokenPredicate predicate : getPredicates()) {

            boolean applicable = clause.getKnownPredicates().contains(predicate);
            // possible predicates depend on placement of terms within the query.
            //  this state can't be assigned to the terms as they are immutable and
            //   re-used across multiple queries at any given time.
            applicable |= clause.getPossiblePredicates().contains(predicate)
                    && getContext().getTokenEvaluationEngine().evaluateTerm(predicate, clause.getTerm());

            if (applicable) {

                if (isSynonym(getContext().getTransformedTerms().get(clause))) {

                    LOG.debug("expanding because of " + predicate);
                    expandSynonym(clause, getSynonym(getContext().getTransformedTerms().get(clause)));
                    expanded.add(clause);
                    return;
                }
            }
        }
    }

    private void expandSynonym(final List<LeafClause> replace, final String synonym) {

        LOG.trace("expandSynonym(" + replace + ", " + synonym + ")");
        final LeafClause first = replace.get(0);
        final LeafClause last = replace.get(replace.size() - 1);

        if (first != last) {
            getContext().getTransformedTerms().put(first, "(" + first.getTerm());
            getContext().getTransformedTerms().put(last, last.getTerm()+ " " + synonym + ")");
        } else {
            getContext().getTransformedTerms().put(last, "(" + last.getTerm()+ " " + synonym + ")");
        }
    }

    private void expandSynonym(final LeafClause replace, final String synonym) {

        LOG.trace("expandSynonym(" + replace + ", " + synonym + ")");
        final String originalTerm = getContext().getTransformedTerms().get(replace);
        getContext().getTransformedTerms().put(replace, "(" + originalTerm + " " + synonym + ")");
    }

    private Collection<TokenPredicate> getPredicates() {

        synchronized (this) {
            if (customPredicates == null && predicateNames != null && predicateNames.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList<TokenPredicate>();
                for (String tp : predicateNames) {
                    cp.add(TokenPredicate.valueOf(tp));
                }
                customPredicates = Collections.unmodifiableCollection(cp);
            }
        }
        return predicateNames != null && predicateNames.size() > 0
                ? customPredicates
                : DEFAULT_PREFIXES;
    }


    /** TODO comment me. **/
    public static boolean isTicker(final String string){

        final String s = string.toLowerCase();
        return SYNONYMS.containsKey(s);
    }

    /** TODO comment me. **/
    public static boolean isTickersFullname(final String string){

        final String s = string.toLowerCase();
        return REVERSE_SYNONYMS.containsKey(s);
    }

    /** TODO comment me. **/
    public static boolean isSynonym(final String string) {

        final String s = string.toLowerCase();
        return SYNONYMS.containsKey(s) || REVERSE_SYNONYMS.containsKey(s);
    }

    /** TODO comment me. **/
    public static String getSynonym(final String string) {

        final String s = string.toLowerCase();
        return SYNONYMS.containsKey(s)
                ? SYNONYMS.get(s)
                : REVERSE_SYNONYMS.get(s);
    }

    /** TODO comment me. **/
    public Object clone() throws CloneNotSupportedException {
        final SynonymQueryTransformer retValue = (SynonymQueryTransformer)super.clone();

        retValue.predicateNames = predicateNames;
        retValue.customPredicates = customPredicates;
        retValue.matchingPredicates = new HashSet<TokenPredicate>();
        retValue.builder = new StringBuilder();
        retValue.leafs = new ArrayList<LeafClause>();

        return retValue;
    }

    /** TODO comment me. **/
    public StringBuilder getBuilder() {
        return builder;
    }
}
