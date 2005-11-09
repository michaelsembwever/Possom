package no.schibstedsok.front.searchportal.analyzer;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Properties;

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
    private Properties props;

    public TokenEvaluatorFactory(String query, Map parameters, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: TokenEvaluatorFactory()");
        }
        this.query = query;
        this.parameters = parameters;
        this.regExpEvals = new RegExpEvaluators();
        this.props = properties;
//        fastEvaluator = new FastTokenEvaluator(query);
    }

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

    private TokenEvaluator getFastEvaluator() {
        if (fastEvaluator == null) {
            synchronized (this) {
                // Sonmeone else might already have done this, so check for null again.
                if (fastEvaluator == null) {
                    String host = props.getProperty("tokenevaluator.host");
                    int port = Integer.parseInt(props.getProperty("tokenevaluator.port"));
                    fastEvaluator = new VeryFastTokenEvaluator(
                            HTTPClient.instance("token_evaluator", host, port), query);
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

