/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.Properties;

import no.schibstedsok.front.searchportal.http.HTTPClient;

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

    public interface Context {
        String getQueryString();
        Properties getApplicationProperties();
    }

    private static final TokenEvaluator alwaysTrueEvaluator = new AlwaysTrueTokenEvaluator();

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

    /**
     * FIXME Comment this
     *
     * @param token
     * @return
     * @todo    Simplify. Maybe using different prefixes for different evaluators.
     */
    public TokenEvaluator getEvaluator(final TokenPredicate token) {

        if (token == TokenPredicate.COMPANYSUFFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.WEATHERPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.TVPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.CATALOGUEPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.ALWAYSTRUE ) {
            return alwaysTrueEvaluator;
        } else if (token == TokenPredicate.PHONENUMBER ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.ORGNR ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.PICTUREPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.NEWSPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.MATHPREDICATE ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.WIKIPEDIAPREFIX ) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token == TokenPredicate.FIRSTNAME ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.LASTNAME ) {
            return getFastEvaluator();
//        } else if (token == TokenPredicate.GEO ) { // shouldn't be called as it's a OrPredicate from AnalysisRules
//            return getFastEvaluator();
        } else if (token == TokenPredicate.EXACTWIKI ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.GEOLOCAL ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.GEOGLOBAL ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.COMPANYNAME ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.KEYWORD ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.CATEGORY ) {
            return getFastEvaluator();
//        } else if (token == TokenPredicate.NAMELONGERTHANWIKIPEDIA ) { // FIXME where the hell is this used?
//            return getFastEvaluator();
        } else if (token == TokenPredicate.ENGLISHWORDS ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.PRIOCOMPANYNAME ) {
            return getFastEvaluator();
//        } else if (token == TokenPredicate.EXACT_ ) { // FIXME where the hell is this used?
//            return getFastEvaluator();
        } else if (token == TokenPredicate.FULLNAME ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.WIKIPEDIA ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.TNS ) {
            return getFastEvaluator();
//        } else if (token == TokenPredicate.PICTURE ) { // FIXME where the hell is this used?
//            return getFastEvaluator();
        } else if (token == TokenPredicate.EXACTCOMPANYNAME ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.GEOGLOBALEXACT ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.GEOLOCALEXACT ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.EXACTFIRST ) {
            return getFastEvaluator();
        } else if (token == TokenPredicate.EXACTLAST ) {
            return getFastEvaluator();
        }
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
