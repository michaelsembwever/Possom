// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.RegExpTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/**
 * Mask (inclusively or exclusively) terms in the query that
 * positionally (prefix or anywhere) contains TokenPredicates.
 *
 * <b>Note</b> Using <code>position="prefix" predicates="*_MAGIC"</code> is kinda pointless but is often done anyway.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mick Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class TokenMaskTransformer extends AbstractQueryTransformer {

    /** Position restrictions when searching for matching predicates. **/
    public enum Position {
        /** TODO comment me. **/
        PREFIX,
        /** TODO comment me. **/
        ANY
    };

    /** Types of masking to perform during transformation. **/
    public enum Mask {
        /** TODO comment me. **/
        INCLUDE,
        /** TODO comment me. **/
        EXCLUDE
    };

    private static final Logger LOG = Logger.getLogger(TokenMaskTransformer.class);

    // do not remove token predicates by default any more
    private static final Collection<TokenPredicate> DEFAULT_PREDICATES = Collections.EMPTY_SET;

    private static final String BLANK = "";

    private Collection<String> prefixes = new ArrayList<String>();
    private Collection<TokenPredicate> customPredicates;
    private Position position = Position.ANY;
    private Mask mask = Mask.EXCLUDE;

    private Set<TokenPredicate> insidePredicates = new HashSet<TokenPredicate>();
    private StringBuilder predicateBuilder = new StringBuilder();
    private Map<LeafClause,String> leaves = new HashMap<LeafClause,String>();
    private RegExpEvaluatorFactory regExpFactory = null;

    private static final String ERR_PREFIX_NOT_FOUND = "No such TokenPredicate ";

    /** TODO comment me. **/
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {

        // -->HACK while AlternationRotation is in implementation
        for (TokenPredicate predicate : getPredicates()) {
            if (clause.getPossiblePredicates().contains(predicate)) {
                insidePredicates.add(predicate);
            }
        }
        clause.getFirstClause().accept(this);

        if(insidePredicates.size() > 0
        // <--HACK
                || Position.ANY == position || Mask.INCLUDE == mask){
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. **/
    protected void visitImpl(final PhraseClause clause) {
        // don't remove prefix if it is infact a phrase.
    }

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {

        // Mask.INCLUDE masks out everything by default
        final String transformedTerm = getContext().getTransformedTerms().get(clause);
        if(Mask.INCLUDE == mask){
            getContext().getTransformedTerms().put(clause, BLANK);
        }

        // Do not remove if the query is just the prefix and we're in prefix exclude mode.
        if (Position.ANY == position || Mask.INCLUDE == mask || getContext().getQuery().getTermCount() > 1) {

            // -->HACK while AlternationRotation is in implementation
            if(insidePredicates.size() > 0){

                if(clause.getField() != null){
                    clearInsidePrefixState();
                }else{
                    if(predicateBuilder.length()>0){
                        predicateBuilder.append(' ');
                    }
                    predicateBuilder.append(clause.getTerm());
                    leaves.put(clause, getContext().getTransformedTerms().get(clause));
                }
                for(TokenPredicate predicate : insidePredicates){

                    final TokenEvaluator eval = regExpFactory.getEvaluator(predicate);
                    // HACK. if it isn't a RegExpTokenEvaluator it won't remove the prefix.
                    if(eval instanceof RegExpTokenEvaluator
                            && ((RegExpTokenEvaluator)eval).evaluateToken(null, predicateBuilder.toString(), null, true)){

                        for(LeafClause c : leaves.keySet()){
                            getContext().getTransformedTerms().put(c, Mask.INCLUDE == mask ? leaves.get(c) : BLANK);
                        }
                    }
                }
            }
            // <--HACK

            boolean check = Position.ANY == position;
            check |= Position.PREFIX == position && clause == getContext().getQuery().getFirstLeafClause();

            if (check) {
                for (TokenPredicate predicate : getPredicates()) {

                    boolean transform = clause.getPossiblePredicates().contains(predicate);
                    transform &= predicate.evaluate(getContext().getTokenEvaluatorFactory()); // XXX maybe not needed
                    transform |= clause.getKnownPredicates().contains(predicate);

                    if (transform) {

                        getContext().getTransformedTerms().put(clause, Mask.INCLUDE == mask ? transformedTerm : BLANK);
                        return;
                    }
                }
            }
        }
    }

    private void clearInsidePrefixState(){
        // reset. not that it will be used again anyway ;-)
        insidePredicates.clear();
        predicateBuilder.setLength(0);
        leaves.clear();
    }

    private Collection<TokenPredicate> getPredicates() {
        synchronized (this) {
            if (customPredicates == null && prefixes != null && prefixes.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList(DEFAULT_PREDICATES);
                for (String tp : prefixes) {
                    try{
                        cp.add(TokenPredicate.valueOf(tp));
                    }catch(IllegalArgumentException iae){
                        LOG.error(ERR_PREFIX_NOT_FOUND + tp, iae);
                    }
                }
                customPredicates = Collections.unmodifiableCollection(cp);
            }
        }
        return prefixes != null && prefixes.size() > 0
                ? customPredicates
                : DEFAULT_PREDICATES;
    }

    /** TODO comment me. **/
    public Object clone() throws CloneNotSupportedException {

        final TokenMaskTransformer retValue = (TokenMaskTransformer)super.clone();
        retValue.customPredicates = customPredicates;

        retValue.prefixes = prefixes;
        retValue.insidePredicates = new HashSet<TokenPredicate>();
        retValue.predicateBuilder = new StringBuilder();
        retValue.leaves = new HashMap<LeafClause,String>();
        retValue.position = position;
        retValue.mask = mask;

        return retValue;
    }

    /** TODO comment me. **/
    public void setContext(final QueryTransformer.Context cxt) {

        super.setContext(cxt);

        final RegExpEvaluatorFactory.Context regExpEvalFactory = ContextWrapper.wrap(
                RegExpEvaluatorFactory.Context.class, cxt);

        regExpFactory = RegExpEvaluatorFactory.valueOf(regExpEvalFactory);
    }

    /** TODO comment me. **/
    public void addPredicates(final String[] pArr) {

        if(pArr.length > 0 && pArr[0].trim().length() >0){
            prefixes.addAll(Arrays.asList(pArr));
        }
    }

    /**
     * Getter for property position.
     *
     * @return Value of property position.
     */
    public Position getMatch() {
        return position;
    }

    /**
     * Setter for property position.
     *
     * @param position New value of property position.
     */
    public void setMatch(final Position position) {
        this.position = position;
    }

    /**
     * Getter for property mask.
     * @return Value of property mask.
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Setter for property mask.
     * @param mask New value of property mask.
     */
    public void setMask(final Mask mask) {
        this.mask = mask;
    }


}