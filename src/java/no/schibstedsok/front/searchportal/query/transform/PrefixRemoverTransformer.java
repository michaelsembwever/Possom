// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mick Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class PrefixRemoverTransformer extends AbstractQueryTransformer {
    
    private static final Collection<TokenPredicate> DEFAULT_PREFIXES = Collections.unmodifiableCollection(
            Arrays.asList( 
                TokenPredicate.SITEPREFIX,
                TokenPredicate.CATALOGUEPREFIX,
                TokenPredicate.PICTUREPREFIX,
                TokenPredicate.SKIINFOPREFIX,
                TokenPredicate.NEWSPREFIX,
                TokenPredicate.NEWSPREFIX,
                TokenPredicate.WIKIPEDIAPREFIX,
                TokenPredicate.TVPREFIX,
                TokenPredicate.WEATHERPREFIX
            ));

    private static final String BLANK = "";
    
    private Collection<String> prefixes = new ArrayList<String>();
    private Collection<TokenPredicate> customPrefixes;
    
    private Set<TokenPredicate> insidePrefixes = new HashSet<TokenPredicate>();
    private StringBuilder prefixBuilder = new StringBuilder();
    private List<LeafClause> leafList = new ArrayList<LeafClause>();
    
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    protected void visitImpl(final DefaultOperatorClause clause) {
        for (TokenPredicate predicate : getPrefixes()) {
            if ( clause.getPossiblePredicates().contains(predicate) ) {
                insidePrefixes.add(predicate);
            }
        }
        clause.getFirstClause().accept(this);
        if( insidePrefixes.size() > 0 ){
            clause.getSecondClause().accept(this);
        }
    }
    
    protected void visitImpl(final PhraseClause clause) {
        // don't remove prefix if it is infact a phrase.
    }
    
    protected void visitImpl(final LeafClause clause) {
        // Do not remove if the query is just the prefix.
        if (getContext().getQuery().getTermCount() > 1) {

            if( insidePrefixes.size() > 0 ){
                if(prefixBuilder.length()>0){
                    prefixBuilder.append(' ');
                }
                if(clause.getField() != null){
                    clearInsidePrefixState();
                }else{
                    prefixBuilder.append(clause.getTerm());
                    leafList.add(clause);
                }
                for(TokenPredicate predicate : insidePrefixes){

                    final RegExpEvaluatorFactory.Context cxt = ContextWrapper.wrap(
                            RegExpEvaluatorFactory.Context.class, getContext());
                    final RegExpEvaluatorFactory factory = RegExpEvaluatorFactory.valueOf(cxt);

                    if( factory.getEvaluator(predicate).evaluateToken(null, prefixBuilder.toString(), null, true) ){
                        for(LeafClause c : leafList){
                            getContext().getTransformedTerms().put(c, BLANK);
                        }
                    }
                }
            }

            if (clause == getContext().getQuery().getFirstLeafClause()) {
                for (TokenPredicate predicate : getPrefixes()) {

                    if (clause.getPossiblePredicates().contains(predicate)
                            || clause.getKnownPredicates().contains(predicate)) {

                        getContext().getTransformedTerms().put(clause, BLANK);
                        return;
                    }
                }
            }
        }
    }

    private void clearInsidePrefixState(){
        // reset. not that it will be used again anyway ;-)
        insidePrefixes.clear();
        prefixBuilder.setLength(0);
        leafList.clear();
    }
    
    private Collection<TokenPredicate> getPrefixes() {
        synchronized (this) {
            if (customPrefixes == null && prefixes != null && prefixes.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList();
                for (String tp : prefixes) {
                    cp.add(TokenPredicate.valueOf(tp));
                }
                customPrefixes = Collections.unmodifiableCollection(cp);
            }
        }
        return prefixes != null && prefixes.size() > 0
                ? customPrefixes
                : DEFAULT_PREFIXES;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final PrefixRemoverTransformer retValue = (PrefixRemoverTransformer)super.clone();
        retValue.customPrefixes = customPrefixes;

        retValue.prefixes = new ArrayList<String>();
        retValue.insidePrefixes = new HashSet<TokenPredicate>();
        retValue.prefixBuilder = new StringBuilder();
        retValue.leafList = new ArrayList<LeafClause>();
        
        return retValue;
    }
}