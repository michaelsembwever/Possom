/*
 * Copyright (2005-2007) Schibsted Søk AS
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
package no.sesat.search.query.token;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.site.Site;
import org.apache.log4j.Logger;

/**
 * TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * This class is not synchronised (Except for the evaluateTerm method).
 * Manual synhronisation must be taken when calling operate or setter methods from inside SearchCommand classes.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version $Id$
 */
public final class TokenEvaluationEngineImpl implements TokenEvaluationEngine {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /** TODO comment me. **/
    static final TokenEvaluator ALWAYS_TRUE_EVALUATOR = new TokenEvaluator(){
        public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {
            return true;
        }
        public boolean isQueryDependant(final TokenPredicate predicate) {
            return false;
        }
    };

    /** TODO comment me. **/
    static final TokenEvaluator ALWAYS_FALSE_EVALUATOR = new TokenEvaluator(){
        public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {
            return false;
        }
        public boolean isQueryDependant(final TokenPredicate predicate) {
            return false;
        }
    };

    private TokenEvaluator fastEvaluator;
    private final JepTokenEvaluator jedEvaluator;

    private static final Logger LOG = Logger.getLogger(TokenEvaluationEngineImpl.class);
    private static final String ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR = "Failed to construct the fast evaluator";
    private static final String ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED =
            "Interrupted waiting for FastEvaluatorCreator. Evaluation on this query will fail.";
    private static final String ERR_TOKENTYPE_WIHOUT_IMPL = "Token type not known or implemented. ";
    private static final String ERR_GENERIC_TOKENTYPE_WIHOUT_IMPL = "Generic token type not known or implemented. ";
    private static final String DEBUG_POOL_COUNT = "Pool size: ";

    private final Context context;
    private final Future fastEvaluatorCreator;

    /**
     * Holds value of property state.
     */
    private State state;

    private volatile Thread owningThread = Thread.currentThread();

    /**
     * Create a new TokenEvaluationEngine.
     *
     * @param query
     * @param params
     * @param properties
     */
    public TokenEvaluationEngineImpl(final Context cxt) {


        if(LOG.isDebugEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
            LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
        }

        context = cxt;
        fastEvaluatorCreator = EXECUTOR.submit(new FastEvaluatorCreator());

        jedEvaluator = new JepTokenEvaluator(context.getQueryString());
    }

    /** @inherit **/
    public TokenEvaluator getEvaluator(final TokenPredicate token) throws VeryFastListQueryException {

        switch(token.getType()){
            case GENERIC:
                switch(token){
                    case ALWAYSTRUE:
                        return ALWAYS_TRUE_EVALUATOR;
                    default:
                        throw new IllegalArgumentException(ERR_GENERIC_TOKENTYPE_WIHOUT_IMPL + token);
                }
            case FAST:
                return getFastEvaluator();
            case REGEX:
                return RegExpEvaluatorFactory.valueOf(
                        ContextWrapper.wrap(RegExpEvaluatorFactory.Context.class,context)).getEvaluator(token);
            case JEP:
                return jedEvaluator;
            default:
                throw new IllegalArgumentException(ERR_TOKENTYPE_WIHOUT_IMPL + token);
        }
    }

    /** @inherit **/
    public String getQueryString() {
        return context.getQueryString();
    }

    private TokenEvaluator getFastEvaluator() throws VeryFastListQueryException {

        try {
            fastEvaluatorCreator.get();

        } catch (InterruptedException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
            throw new VeryFastListQueryException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, ex);
        } catch (ExecutionException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
            throw new VeryFastListQueryException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, ex);
        }
        if( null == fastEvaluator ){
            throw new VeryFastListQueryException(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR, new NullPointerException());
        }

        return fastEvaluator;
    }

    /** @inherit **/
    public Site getSite() {
        return context.getSite();
    }

    /** @inherit **/
    public synchronized boolean evaluateTerm(final TokenPredicate predicate, final String term) {

        return evaluateImpl(predicate, new EvaluationState(term, Collections.EMPTY_SET, Collections.EMPTY_SET));
    }

    /** @inherit **/
    public synchronized boolean evaluateClause(final TokenPredicate predicate, final Clause clause) {

        return evaluateImpl(predicate, new EvaluationState(clause));
    }

    /** @inherit **/
    public synchronized boolean evaluateQuery(final TokenPredicate predicate, final Query query) {

        return evaluateImpl(predicate, query.getEvaluationState());
    }

    private boolean evaluateImpl(
            final TokenPredicate predicate, final State state) {

        final Thread origThread = owningThread;
        try{
            // setup the engine's required state before any evaluation process
            setState(state);
            // temporarily change owningThread to allow this thread to evaluate
            owningThread = Thread.currentThread();
            // run the evaluation process
            return predicate.evaluate(this);

        }finally{
            setState(null);
            owningThread = origThread;
        }
    }

    public synchronized Thread getOwningThread() {
        return owningThread;
    }

    private final class FastEvaluatorCreator implements Runnable{
        public void run() {

            try {

                fastEvaluator = new VeryFastTokenEvaluator(
                        ContextWrapper.wrap(VeryFastTokenEvaluator.Context.class, context));

            } catch (VeryFastListQueryException ex) {
                LOG.error(ERR_FAILED_CONSTRUCTING_FAST_EVALUATOR);
            }
        }

    }

    /**
     * Getter for property state.
     * @return Value of property state.
     */
    public State getState() {
        return state;
    }

    /**
     * Setter for property state.
     * @param state New value of property state.
     */
    public void setState(final State state) {
        this.state = state;
    }

}
