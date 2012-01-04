/*
 * Copyright (2005-2012) Schibsted ASA
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
package no.sesat.search.query.token;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.site.Site;
import org.apache.log4j.Logger;

/**
 * TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * This class is not synchronised (Except for the evaluateTerm, evaluateClause, and evaluateQuery methods).
 * Manual synhronisation must be taken when calling operate or setter methods from inside SearchCommand classes.
 *
 *
 * @version $Id$
 */
public class TokenEvaluationEngineImpl implements TokenEvaluationEngine {

    // Constants -----------------------------------------------------

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(TokenEvaluationEngineImpl.class);
    private static final String ERR_TOKENTYPE_WIHOUT_IMPL = "Token type not known or implemented. ";
    private static final String DEBUG_POOL_COUNT = "Pool size: ";
    private static final String ERR_METHOD_CLOSED_TO_OTHER_THREADS
            = "TokenPredicate.evaluate(..) can only be used by same thread that created TokenEvaluationEngine!";
    private static final String ERR_ENGINE_MISSING_STATE = "TokenEvaluationEngine must have state assigned";

    // Attributes -----------------------------------------------------

    private final Context context;
    private State state;
    private volatile Thread owningThread = Thread.currentThread();


    private final transient Map<TokenPredicate,TokenEvaluator> evaluatorCache
            = new HashMap<TokenPredicate,TokenEvaluator>();

    /** threading lock to the cache maps since they are not synchronised,
     * and it's overkill to make them Hashtables. **/
    private final transient ReentrantReadWriteLock evaluatorCacheGate = new ReentrantReadWriteLock();

    // Constructors -----------------------------------------------------

    /**
     * Create a new TokenEvaluationEngine.
     *
     * @param cxt context to work within.
     */
    public TokenEvaluationEngineImpl(final Context cxt) {


        if(LOG.isDebugEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
            LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
        }

        context = cxt;
    }

    // Public -----------------------------------------------------

    @Override
    public TokenEvaluator getEvaluator(final TokenPredicate token) throws EvaluationException {

        TokenEvaluator result = null;

        try{
            evaluatorCacheGate.readLock().lock();

            result = evaluatorCache.get(token);

        }finally{
            evaluatorCacheGate.readLock().unlock();
        }

        if(null == result){

            try{
                evaluatorCacheGate.writeLock().lock();


                for(EvaluatorType type : EvaluatorType.getInstances()){

                    final AbstractEvaluatorFactory factory = AbstractEvaluatorFactory.instanceOf(
                            ContextWrapper.wrap(
                            AbstractEvaluatorFactory.Context.class,
                            context,
                            type
                    ));

                    if(factory.isResponsibleFor(token)){
                        LOG.trace("Evaluator for " + token + " found by " + type.getEvaluatorFactoryClassName());
                        result = factory.getEvaluator(token);
                        break;
                    }
                }

                if(null == result){
                    // no evaluator has been defined for this predicate. it must always be false then.
                    result = ALWAYS_FALSE_EVALUATOR;
                }
                evaluatorCache.put(token, result);

            }finally{
                evaluatorCacheGate.writeLock().unlock();
            }
        }
        return result;
    }

    @Override
    public String getQueryString() {
        return context.getQueryString();
    }

    @Override
    public Site getSite() {
        return context.getSite();
    }

    @Override
    public synchronized boolean evaluateTerm(
            final TokenPredicate predicate,
            final String term) throws EvaluationException{

        return evaluateInAnyThread(predicate, new EvaluationState(term, Collections.EMPTY_SET, Collections.EMPTY_SET));
    }

    @Override
    public synchronized boolean evaluateClause(
            final TokenPredicate predicate,
            final Clause clause) throws EvaluationException{

        return evaluateInAnyThread(predicate, new EvaluationState(clause));
    }

    @Override
    public synchronized boolean evaluateQuery(
            final TokenPredicate predicate,
            final Query query) throws EvaluationException{

        return evaluateInAnyThread(predicate, query.getEvaluationState());
    }

    @Override
    public boolean evaluate(final TokenPredicate token) throws EvaluationException{

        // process
        if(Thread.currentThread() != getOwningThread()){
            throw new IllegalStateException(ERR_METHOD_CLOSED_TO_OTHER_THREADS);
        }

        // check that the evaluation hasn't already been done
        // we can only check against the knownPredicates because with the possiblePredicates we are not sure whether
        //  the evaluation is for the building of the known and possible predicate list
        //    (during query parsing)(in which
        //  case we could perform the check) or if we are scoring and need to know if the
        //    possible predicate is really
        //  applicable now (in the context of the whole query).
        final Set<TokenPredicate> knownPredicates = getState().getKnownPredicates();
        if(null != knownPredicates && knownPredicates.contains(token)){
            return true;
        }

        final TokenEvaluator evaluator = getEvaluator(token);

        if(null != getState().getTerm()){

            // Single term or clause evaluation
            return evaluator.evaluateToken(token, getState().getTerm(), getQueryString());

        }else if(null != getState().getQuery()){

            // Whole query evaluation
            return getState().getPossiblePredicates().contains(token)
                    && evaluator.evaluateToken(token, null, getQueryString());

        }

        throw new IllegalStateException(ERR_ENGINE_MISSING_STATE);
    }


    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    // private -----------------------------------------------------

    private boolean evaluateInAnyThread(
            final TokenPredicate predicate,
            final State state) throws EvaluationException{

        final Thread origThread = owningThread;
        try{
            // setup the engine's required state before any evaluation process
            setState(state);
            // temporarily change owningThread to allow this thread to evaluate
            owningThread = Thread.currentThread();
            // run the evaluation process
            return evaluate(predicate);

        }finally{
            setState(null);
            owningThread = origThread;
        }
    }

    private synchronized Thread getOwningThread() {
        return owningThread;
    }

    // inner classes -----------------------------------------------------

}
