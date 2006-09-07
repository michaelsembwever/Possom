/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.token;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;

/**
 * TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * This class is not synchronised (Except for the evaluateTerm method).
 * Manual synhronisation must be taken when calling operate or setter methods from inside SearchCommand classes.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class TokenEvaluationEngineImpl implements TokenEvaluationEngine {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final TokenEvaluator ALWAYS_TRUE_EVALUATOR = new TokenEvaluator(){
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
    private static final String ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED =
            "Interrupted waiting for FastEvaluatorCreator. Analysis on this query will fail.";
    private static final String ERR_TOKENTYPE_WIHOUT_IMPL = "Token type not known or implemented. ";
    private static final String ERR_GENERIC_TOKENTYPE_WIHOUT_IMPL = "Generic token type not known or implemented. ";
    private static final String ERR_FAILED_CREATING_EVAL = "Failed to create VeryFastTokenEvaluator";

    private final Context context;
    private final Future fastEvaluatorCreator;

    /** The current term the parser is on **/
    private String currTerm = null;

    private Set<TokenPredicate> knownPredicates;
    private Set<TokenPredicate> possiblePredicates;

    private Locale locale;
    
    private volatile Thread owningThread = Thread.currentThread();

    /**
     * Create a new TokenEvaluationEngine.
     *
     * @param query
     * @param params
     * @param properties
     */
    public TokenEvaluationEngineImpl(final Context cxt) {
        context = cxt;
        fastEvaluatorCreator = EXECUTOR.submit(new FastEvaluatorCreator());

        jedEvaluator = new JepTokenEvaluator(context.getQueryString());
    }

    /** @inherit **/
    public TokenEvaluator getEvaluator(final TokenPredicate token) {

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

    private TokenEvaluator getFastEvaluator() {
        try {
            fastEvaluatorCreator.get();
        } catch (InterruptedException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
        } catch (ExecutionException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex);
        }
        return fastEvaluator;
    }

    /** TODO comment me. **/
    public void setState(
            final String currentTerm,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates){

        setCurrentTerm(currentTerm);
        setClausesKnownPredicates(knownPredicates);
        setClausesPossiblePredicates(possiblePredicates);
    }

    /** @inherit **/
    public void setCurrentTerm(final String term) {
        currTerm = term;
    }

    /** @inherit **/
    public String getCurrentTerm() {
        return currTerm;
    }

    /** @inherit **/
    public void setClausesKnownPredicates(final Set<TokenPredicate> _knownPredicates) {
        knownPredicates = _knownPredicates;
    }

    /** @inherit **/
    public Set<TokenPredicate> getClausesKnownPredicates() {
        return knownPredicates;
    }

    /** @inherit **/
    public void setClausesPossiblePredicates(final Set<TokenPredicate> _possiblePredicates) {
        possiblePredicates = _possiblePredicates;
    }

    /** @inherit **/
    public Set<TokenPredicate> getClausesPossiblePredicates() {
        return possiblePredicates;
    }

    /** @inherit **/
    public Site getSite() {
        return context.getSite();
    }

    /** @inherit **/
    public synchronized boolean evaluateTerm(final TokenPredicate predicate, final String term) {
        
        final Thread origThread = owningThread;
        try{
            // setup the engine's required state before any evaluation process
            setState(term, Collections.EMPTY_SET, Collections.EMPTY_SET);
            // temporarily change owningThread to allow this thread to evaluate
            owningThread = Thread.currentThread();
            // run the evaluation process
            return predicate.evaluate(this);
            
        }finally{
            setCurrentTerm(null);
            owningThread = origThread;
        }
    }

    public synchronized Thread getOwningThread() {
        return owningThread;
    }

    private final class FastEvaluatorCreator implements Runnable{
        public void run() {

            fastEvaluator = new VeryFastTokenEvaluator(
                    ContextWrapper.wrap(VeryFastTokenEvaluator.Context.class, context));
        }

    }

}
