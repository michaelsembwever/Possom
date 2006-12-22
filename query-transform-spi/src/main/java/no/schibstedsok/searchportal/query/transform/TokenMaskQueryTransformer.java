// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Mask (inclusively or exclusively) terms in the query that
 * positionally (prefix or anywhere) contains TokenPredicates.
 *
 * <b>Note</b> Using <code>position="prefix" predicates="*_MAGIC"</code> is kinda pointless but is often done anyway.
 * <b>Note</b> position="prefix" only currently works with single terms. XXX
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mick Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class TokenMaskQueryTransformer extends AbstractQueryTransformer {

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

    private static final Logger LOG = Logger.getLogger(TokenMaskQueryTransformer.class);

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
    protected void visitImpl(final DoubleOperatorClause clause) {

        clause.getFirstClause().accept(this);

        if(Position.ANY == position || Mask.INCLUDE == mask){
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. **/
    boolean insideMaskClause = false;

    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {

        if(maskClause(clause)){ // XXX must ensure that this won't ignore children's fields
            insideMaskClause = true;
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
            insideMaskClause = false;
        }else{

            clause.getFirstClause().accept(this);

            if(Position.ANY == position || Mask.INCLUDE == mask){
                clause.getSecondClause().accept(this);
            }
        }
    }

    /** don't remove prefix if it is in fact a phrase. **/
    protected void visitImpl(final PhraseClause clause) {}

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {

        // Mask.INCLUDE masks out everything by default
        final String transformedTerm = getContext().getTransformedTerms().get(clause);
        if(Mask.INCLUDE == mask){
            getContext().getTransformedTerms().put(clause, BLANK);
        }

        // Do not remove if the query is just the prefix and we're in prefix exclude mode.
        if (Mask.INCLUDE == mask || getContext().getQuery().getTermCount() > 1) {

            if(maskField(clause)){
                // this resets the the term to the clause's field or term
                getContext().getTransformedTerms().put(clause,
                        Mask.INCLUDE == mask ? clause.getField() : clause.getTerm());

            }else if(insideMaskClause || maskClause(clause)){

                getContext().getTransformedTerms().put(clause, Mask.INCLUDE == mask ? transformedTerm : BLANK);
            }
        }
    }

    /** TODO comment me. **/
    protected boolean maskClause(final Clause clause){

        boolean transform = false;

        boolean check = Position.ANY == position;
        check |= Position.PREFIX == position && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : getPredicates()) {

                if (engine.evaluateClause(predicate, clause)) {
                    transform = true;
                    break;
                }
            }
        }

        return transform;
    }

    /** TODO comment me. **/
    protected boolean maskField(final LeafClause clause){

        boolean transform = false;

        boolean check = Position.ANY == position;
        check |= Position.PREFIX == position && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : getPredicates()) {

                // if the field is the token then mask the field and include the term.

                if(null != clause.getField()){

                    if(engine.evaluateTerm(predicate, clause.getField())){
                        transform = true;
                        break;
                    }
                }
            }
        }

        return transform;
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
    @Override
    public Object clone() throws CloneNotSupportedException {

        final TokenMaskQueryTransformer retValue = (TokenMaskQueryTransformer)super.clone();
        retValue.customPredicates = customPredicates;

        retValue.prefixes = prefixes;
        retValue.insidePredicates = new HashSet<TokenPredicate>();
        retValue.predicateBuilder = new StringBuilder();
        retValue.leaves = new HashMap<LeafClause,String>();
        retValue.position = position;
        retValue.mask = mask;

        return retValue;
    }

    @Override
    public QueryTransformer readQueryTransformer(final Element qt){
        
        super.readQueryTransformer(qt);
        addPredicates(qt.getAttribute("predicates").split(","));
        if(qt.getAttribute("match").length() > 0){
            setMatch(Position.valueOf(qt.getAttribute("position").toUpperCase()));
        }
        if(qt.getAttribute("mask").length() >0){
            setMask(Mask.valueOf(qt.getAttribute("mask").toUpperCase()));
        }     
        return this;
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
