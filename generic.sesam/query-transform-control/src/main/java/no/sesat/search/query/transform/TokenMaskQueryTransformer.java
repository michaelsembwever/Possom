/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.BinaryClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.PhraseClause;
import no.sesat.search.query.token.EvaluationException;
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
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class TokenMaskQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(TokenMaskQueryTransformer.class);

    private static final String BLANK = "";


    private Set<TokenPredicate> insidePredicates = new HashSet<TokenPredicate>();
    private StringBuilder predicateBuilder = new StringBuilder();
    private Map<LeafClause,String> leaves = new HashMap<LeafClause,String>();

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
    protected void visitImpl(final BinaryClause clause) {

        clause.getFirstClause().accept(this);

        if(Position.ANY == config.getPosition() || Mask.INCLUDE == config.getMask()){
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

            if(Position.ANY == config.getPosition() || Mask.INCLUDE == config.getMask()){
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

        boolean check = Position.ANY == config.getPosition();
        check |= Position.PREFIX == config.getPosition() && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : config.getPredicates()) {
                try{
                    if (engine.evaluateClause(predicate, clause)) {
                        transform = true;
                        break;
                    }
                }catch(EvaluationException ie){
                    LOG.error("failed to check predicate" + predicate +" with evaluateClause " + clause);
                }
            }
        }

        return transform;
    }

    /** TODO comment me. **/
    protected boolean maskField(final LeafClause clause){

        boolean transform = false;

        boolean check = Position.ANY == config.getPosition();
        check |= Position.PREFIX == config.getPosition() && clause == getContext().getQuery().getFirstLeafClause();

        if (check) {
            final TokenEvaluationEngine engine = getContext().getTokenEvaluationEngine();

            for (TokenPredicate predicate : config.getPredicates()) {

                // if the field is the token then mask the field and include the term.

                if(null != clause.getField()){
                    try{
                        if(engine.evaluateTerm(predicate, clause.getField())){
                            transform = true;
                            break;
                        }
                    }catch(EvaluationException ie){
                        LOG.error("failed to check predicate" + predicate +" with evaluateTerm " + clause.getField());
                    }
                }
            }
        }

        return transform;
    }
}
