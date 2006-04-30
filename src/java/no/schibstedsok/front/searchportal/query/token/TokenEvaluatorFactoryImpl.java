/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.token;

import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import org.apache.commons.collections.Predicate;
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

    public interface Context extends BaseContext, QueryStringContext, RegExpEvaluatorFactory.Context, 
            SiteConfiguration.Context{
    }
    
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    
    private static final TokenEvaluator ALWAYS_TRUE_EVALUATOR = new AlwaysTrueTokenEvaluator();

    private TokenEvaluator fastEvaluator;
    private final JepTokenEvaluator jedEvaluator;
    
    private static final Log LOG = LogFactory.getLog(TokenEvaluatorFactoryImpl.class);
    private static final String ERR_FAST_EVALUATOR_CREATOR_INTERRUPTED = 
            "Interrupted waiting for FastEvaluatorCreator. Analysis on this query will fail.";
    private static final String ERR_TOKENTYPE_WIHOUT_IMPL = "Token type not known or implemented. ";
    private static final String ERR_GENERIC_TOKENTYPE_WIHOUT_IMPL = "Generic token type not known or implemented. ";

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
                return RegExpEvaluatorFactory.valueOf(context).getEvaluator(token);
            case JEP:
                return jedEvaluator;
            default:
                throw new IllegalArgumentException(ERR_TOKENTYPE_WIHOUT_IMPL + token);
        }
    }

    public String getQueryString() {
        return context.getQueryString();
    }

    protected Properties getProperties() {
        return SiteConfiguration.valueOf(context).getProperties();
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

    private final class FastEvaluatorCreator implements Runnable{
        public void run() {
            final String host = getProperties().getProperty("tokenevaluator.host");
            final int port = Integer.parseInt(getProperties().getProperty("tokenevaluator.port"));

            fastEvaluator = new VeryFastTokenEvaluator(
                    HTTPClient.instance("token_evaluator", host, port), getQueryString());
        }
        
    }

}
