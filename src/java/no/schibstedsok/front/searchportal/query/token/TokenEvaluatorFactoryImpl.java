/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.token;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class TokenEvaluatorFactoryImpl implements TokenEvaluatorFactory {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    
    private static final TokenEvaluator ALWAYS_TRUE_EVALUATOR = new TokenEvaluator(){
        public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {
            return true;
        }
        public boolean isQueryDependant(final TokenPredicate predicate) {
            return false;
        }        
    };
    
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
    
    private static final Log LOG = LogFactory.getLog(TokenEvaluatorFactoryImpl.class);
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

    /**
     * Create a new TokenEvaluatorFactory.
     *
     * @param query
     * @param params
     * @param properties
     */
    public TokenEvaluatorFactoryImpl(final Context cxt) {
        context = cxt;
        fastEvaluatorCreator = EXECUTOR.submit( new FastEvaluatorCreator() );
        
        jedEvaluator = new JepTokenEvaluator(context.getQueryString());
    }

    /** Find or create the TokenEvaluator that will evaluate if given (Token)Predicate is true.
     *
     * @param token
     * @return
     */
    public TokenEvaluator getEvaluator(final TokenPredicate token) {

        switch( token.getType() ){
            case GENERIC:
                switch( token ){
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

    public String getQueryString() {
        return context.getQueryString();
    }

    private TokenEvaluator getFastEvaluator() {
        try {
            fastEvaluatorCreator.get();
        } catch (InterruptedException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex );
        } catch (ExecutionException ex) {
            LOG.error(ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED, ex );
        }
        return fastEvaluator;
    }

    public void setCurrentTerm(final String term) {
        currTerm = term;
    }

    public String getCurrentTerm() {
        return currTerm;
    }

    public void setClausesKnownPredicates(final Set<TokenPredicate> _knownPredicates) {
        knownPredicates = _knownPredicates;
    }

    public Set<TokenPredicate> getClausesKnownPredicates() {
        return knownPredicates;
    }

    public void setClausesPossiblePredicates(final Set<TokenPredicate> _possiblePredicates) {
        possiblePredicates = _possiblePredicates;
    }

    public Set<TokenPredicate> getClausesPossiblePredicates() {
        return possiblePredicates;
    }

    public Site getSite() {
        return context.getSite();
    }

    private final class FastEvaluatorCreator implements Runnable{
        public void run() {
            
            fastEvaluator = new VeryFastTokenEvaluator(
                    ContextWrapper.wrap(VeryFastTokenEvaluator.Context.class, context));
        }
        
    }

}
