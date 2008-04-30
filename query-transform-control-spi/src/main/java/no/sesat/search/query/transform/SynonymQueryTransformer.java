/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * SynonymQueryTransformer.java
 *
 * Created on April 5, 2006, 8:05 PM
 *
 */

package no.sesat.search.query.transform;

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

import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
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
                (TokenPredicate)TokenPredicate.Categories.STOCKMARKETFIRMS,
                (TokenPredicate)TokenPredicate.Categories.STOCKMARKETTICKERS
            ));

    /** Synonym expansion are only performed for clauses matching the predicates
     * contained in predicateNames */
    private Collection<String> predicateNames = new ArrayList<String>();
    private Collection<TokenPredicate> customPredicates;

    private List<LeafClause> leafs = new ArrayList<LeafClause>();
    private Set<TokenPredicate> matchingPredicates = new HashSet<TokenPredicate>();
    private List<LeafClause> expanded = new ArrayList<LeafClause>();

    private StringBuilder builder = new StringBuilder();

    /**
     *
     * @param config
     */
    public SynonymQueryTransformer(final QueryTransformerConfig config){
    }

    /** TODO comment me. **/
    public void addPredicateName(final String name) {
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

        LOG.trace("expandSynonym(" + replace + ", " + synonym + ')');
        final LeafClause first = replace.get(0);
        final LeafClause last = replace.get(replace.size() - 1);

        if (first != last) {
            getContext().getTransformedTerms().put(first, '(' + first.getTerm());
            getContext().getTransformedTerms().put(last, last.getTerm() + ' ' + synonym + ')');
        } else {
            getContext().getTransformedTerms().put(last, '(' + last.getTerm()+ ' ' + synonym + ')');
        }
    }

    private void expandSynonym(final LeafClause replace, final String synonym) {

        LOG.trace("expandSynonym(" + replace + ", " + synonym + ')');
        final String originalTerm = getContext().getTransformedTerms().get(replace);
        getContext().getTransformedTerms().put(replace, '(' + originalTerm + ' ' + synonym + ')');
    }

    private Collection<TokenPredicate> getPredicates() {

        synchronized (this) {
            if (customPredicates == null && predicateNames != null && predicateNames.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList<TokenPredicate>();
                for (String tp : predicateNames) {
                    cp.add(TokenPredicate.Static.getTokenPredicate(tp));
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
    public StringBuilder getBuilder() {
        return builder;
    }
}
