/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.Properties;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.parser.QueryStringContext;

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

    public interface Context extends RegExpEvaluatorFactory.Context, QueryStringContext{
        Properties getApplicationProperties();
    }

    private static final TokenEvaluator ALWAYS_TRUE_EVALUATOR = new AlwaysTrueTokenEvaluator();

    private volatile TokenEvaluator fastEvaluator;

    private static final Log LOG = LogFactory.getLog(TokenEvaluatorFactoryImpl.class);

    private final Context context;

    /** The current term the parser is on **/
    private String currTerm = null;

    /**
     * Create a new TokenEvaluatorFactory.
     *
     * @param query
     * @param params
     * @param properties
     */
    public TokenEvaluatorFactoryImpl(final Context cxt) {
        context = cxt;
    }

    /** Find or create the TokenEvaluator that will evaluate if given (Token)Predicate is true.
     *
     * @param token
     * @return
     */
    public TokenEvaluator getEvaluator(final TokenPredicate token) {

        if ( token == TokenPredicate.ALWAYSTRUE ) {
            return ALWAYS_TRUE_EVALUATOR;
        }  else if ( token instanceof TokenPredicate.FastTokenPredicate ) {
            return getFastEvaluator();
        }  else if ( token instanceof TokenPredicate.RegExpTokenPredicate ) {
            return RegExpEvaluatorFactory.valueOf(context).getEvaluator(token);
        }

//        } else if (token == TokenPredicate.GEO ) { // shouldn't be called as it's a OrPredicate from AnalysisRules
//            return getFastEvaluator();
//        } else if (token == TokenPredicate.NAMELONGERTHANWIKIPEDIA ) { // FIXME where the hell is this used?
//            return getFastEvaluator();
//        } else if (token == TokenPredicate.EXACT_ ) { // FIXME where the hell is this used?
//            return getFastEvaluator();
//        } else if (token == TokenPredicate.PICTURE ) { // FIXME where the hell is this used?
//            return getFastEvaluator();

        throw new RuntimeException("Unknown token " + token);
    }

    public String getQueryString() {
        return context.getQueryString();
    }

    protected Properties getProperties() {
        return context.getApplicationProperties();
    }

    private TokenEvaluator getFastEvaluator() {
        if (fastEvaluator == null) {
            synchronized (this) {
                // Sonmeone else might have reached this point, so check for null
                // again.
                // XXX This double-checked locking idiom ONLY works in Java5!
                if (fastEvaluator == null) {
                    final String host = getProperties().getProperty("tokenevaluator.host");
                    final int port = Integer.parseInt(getProperties().getProperty("tokenevaluator.port"));

                    fastEvaluator = new VeryFastTokenEvaluator(
                            HTTPClient.instance("token_evaluator", host, port), getQueryString());
                }
            }
        }
        return fastEvaluator;
    }

    public void setCurrentTerm(final String term) {
        currTerm = term;
    }

    public String getCurrentTerm() {
        return currTerm;
    }
}
