package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.Properties;
import java.io.IOException;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.InfrastructureException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TokenEvaluatorFactory {
    Log log = LogFactory.getLog(TokenEvaluatorFactory.class);

    private String query;
    private RegExpEvaluators regExpEvals;
    private TokenEvaluator fastEvaluator;
    private static TokenEvaluator alwaysTrueEvaluator = new AlwaysTrueTokenEvaluator();
    private Map parameters;

    public TokenEvaluatorFactory(String query, Map parameters) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: TokenEvaluatorFactory()");
        }
        this.query = query;
        this.parameters = parameters;
        this.regExpEvals = new RegExpEvaluators();
//        fastEvaluator = new FastTokenEvaluator(query);
    }

    public TokenEvaluator getEvaluator(String token) {

        if (token.equals("companySuffix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("weatherPrefix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("tvPrefix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("cataloguePrefix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("alwaysTrue")) {
            return alwaysTrueEvaluator;
        } else if (token.equals("phoneNumber")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("orgNr")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("picturePrefix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("newsPrefix")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("mathExpression")) {
            return regExpEvals.getEvaluator(token);
        } else if (token.equals("wikipediaPrefix")) {
            return regExpEvals.getEvaluator(token);
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

    private TokenEvaluator getFastEvaluator() {
        if (fastEvaluator == null) {

            if(log.isInfoEnabled()){
                log.info("getFastEvaluator(): Loading new instance ");
            }
            synchronized (this) {
                try {
                    Properties props = new Properties();
                    props.load(this.getClass().getResourceAsStream("/" + SearchConstants.CONFIGURATION_FILE));

                    log.info("Read configuration from " + SearchConstants.CONFIGURATION_FILE);
                    String host = props.getProperty("tokenevaluator.host");
                    int port = new Integer(props.getProperty("tokenevaluator.port")).intValue();
                    fastEvaluator = new VeryFastTokenEvaluator(
                            HTTPClient.instance("token_evaluator", host, port), query);

                    if(log.isInfoEnabled()){
                        log.info("getFastEvaluator(): host = " + host);
                        log.info("getFastEvalutor(): port = " + port);
                    }
                } catch (IOException e) {
                    log.error("XMLSearchTabsCreator When Reading Configuration from " + SearchConstants.CONFIGURATION_FILE, e);
                    throw new InfrastructureException("Unable to read properties from " + SearchConstants.CONFIGURATION_FILE, e);
                }
            }
        }
        return fastEvaluator;
    }

    public String getQuery() {
        return query;
    }

    public String getParameter(String name) {
        String[] v = (String[]) parameters.get(name);

        if (v != null) {
            return v[0];
        } else {
            return "";
        }
    }
}

