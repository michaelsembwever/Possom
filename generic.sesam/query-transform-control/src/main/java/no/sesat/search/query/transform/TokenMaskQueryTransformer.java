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
package no.sesat.search.query.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.PhraseClause;
import no.sesat.search.query.token.RegExpEvaluatorFactory;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.query.transform.TokenMaskQueryTransformerConfig.Mask;
import no.sesat.search.query.transform.TokenMaskQueryTransformerConfig.Position;
import org.apache.log4j.Logger;

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

    private static final Logger LOG = Logger.getLogger(TokenMaskQueryTransformer.class);

    private static final String BLANK = "";


    private Set<TokenPredicate> insidePredicates = new HashSet<TokenPredicate>();
    private StringBuilder predicateBuilder = new StringBuilder();
    private Map<LeafClause,String> leaves = new HashMap<LeafClause,String>();
    private RegExpEvaluatorFactory regExpFactory = null;

    private static final String ERR_PREFIX_NOT_FOUND = "No such TokenPredicate ";

    private final TokenMaskQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public TokenMaskQueryTransformer(final QueryTransformerConfig config){
        this.config = (TokenMaskQueryTransformerConfig) config;
    }

    /** TODO comment me. **/
    protected void visitImpl(final DoubleOperatorClause clause) {

        clause.getFirstClause().accept(this);

        if(Position.ANY == config.getMatch() || Mask.INCLUDE == config.getMask()){
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

            if(Position.ANY == config.getMatch() || Mask.INCLUDE == config.getMask()){
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
        if(Mask.INCLUDE == config.getMask()){
            getContext().getTransformedTerms().put(clause, BLANK);
        }

        // Do not remove if the query is just the prefix and we're in prefix exclude mode.
        if (Mask.INCLUDE == config.getMask() || getContext().getQuery().getTermCount() > 1) {

            if(maskField(clause)){
                // this resets the the term to the clause's field or term
                getContext().getTransformedTerms().put(clause,
                        Mask.INCLUDE == config.getMask() ? clause.getField() : clause.getTerm());

            }else if(insideMaskClause || maskClause(clause)){

                getContext().getTransformedTerms().put(clause, Mask.INCLUDE == config.getMask() 
                        ? transformedTerm 
                        : BLANK);
            }
        }
    }

    /** TODO comment me. **/
    protected boolean maskClause(final Clause clause){

        boolean transform = false;

        boolean check = Position.ANY == config.getMatch();
        check |= Position.PREFIX == config.getMatch() && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : config.getPredicates()) {

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

        boolean check = Position.ANY == config.getMatch();
        check |= Position.PREFIX == config.getMatch() && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : config.getPredicates()) {

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

    /** TODO comment me. **/
    public void setContext(final QueryTransformer.Context cxt) {

        super.setContext(cxt);

        final RegExpEvaluatorFactory.Context regExpEvalFactory = ContextWrapper.wrap(
                RegExpEvaluatorFactory.Context.class, cxt);

        regExpFactory = RegExpEvaluatorFactory.instanceOf(regExpEvalFactory);
    }



}
