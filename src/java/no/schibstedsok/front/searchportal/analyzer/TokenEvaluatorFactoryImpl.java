/*
 * Copyright (2005) Schibsted S¿k AS
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
public class TokenEvaluatorFactoryImpl implements TokenEvaluatorFactory {
    private static TokenEvaluator alwaysTrueEvaluator = new AlwaysTrueTokenEvaluator();

    private volatile TokenEvaluator fastEvaluator;

    Log log = LogFactory.getLog(TokenEvaluatorFactoryImpl.class);

    private Properties props;

    private String query;

    /**
     * Create a new TokenEvaluatorFactory.
     * 
     * @param query
     * @param params
     * @param properties
     */
    public TokenEvaluatorFactoryImpl(String query, Properties properties) {
        this.query = query;
        this.props = properties;
    }

    /**
     * FIXME Comment this
     * 
     * @param token
     * @return
     * @todo    Simplify. Maybe using different prefixes for different evaluators.
     */
    public TokenEvaluator getEvaluator(String token) {

        if (token.equals("companySuffix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("weatherPrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("tvPrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("cataloguePrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("alwaysTrue")) {
            return alwaysTrueEvaluator;
        } else if (token.equals("phoneNumber")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("orgNr")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("picturePrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("newsPrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("mathExpression")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("wikipediaPrefix")) {
            return RegExpEvaluators.getEvaluator(token);
        } else if (token.equals("firstname")) {
            return getFastEvaluator();
        } else if (token.equals("lastname")) {
            return getFastEvaluator();
        } else if (token.equals("geo")) {
            return getFastEvaluator();
        } else if (token.equals("exactWiki")) {
            return getFastEvaluator();
        } else if (token.equals("geolocal")) {
            return getFastEvaluator();
        } else if (token.equals("geoglobal")) {
            return getFastEvaluator();
        } else if (token.equals("company")) {
            return getFastEvaluator();
        } else if (token.equals("keyword")) {
            return getFastEvaluator();
        } else if (token.equals("category")) {
            return getFastEvaluator();
        } else if (token.equals("nameLongerThanWikipedia")) {
            return getFastEvaluator();
        } else if (token.equals("international")) {
            return getFastEvaluator();
        } else if (token.equals("companypriority")) {
            return getFastEvaluator();
        } else if (token.startsWith("exact_")) {
            return getFastEvaluator();
        } else if (token.equals("fullname")) {
            return getFastEvaluator();
        } else if (token.equals("wikino")) {
            return getFastEvaluator();
        } else if (token.equals("tns")) {
            return getFastEvaluator();
        } else if (token.equals("picture")) {
            return getFastEvaluator();
        } else if (token.equals("exact_firstname")) {
            return getFastEvaluator();
        } else if (token.equals("exact_lastname")) {
            return getFastEvaluator();
        }
        throw new RuntimeException("Unknown token " + token);
    }

    public String getQuery() {
        return query;
    }

    private TokenEvaluator getFastEvaluator() {
        if (fastEvaluator == null) {
            synchronized (this) {
                // Sonmeone else might have reached this point, so check for null
                // again.
                if (fastEvaluator == null) {
                    String host = props.getProperty("tokenevaluator.host");
                    int port = Integer.parseInt(props
                            .getProperty("tokenevaluator.port"));
                    fastEvaluator = new VeryFastTokenEvaluator(HTTPClient
                            .instance("token_evaluator", host, port), query);
                }
            }
        }
        return fastEvaluator;
    }
}
