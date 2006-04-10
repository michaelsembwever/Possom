// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.schibstedsok.front.searchportal.http.HTTPClient;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.front.searchportal.analyzer.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @version $Revision$, $Author$, $Date$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator, ReportingTokenEvaluator {

    private static final Logger LOG = Logger.getLogger(VeryFastTokenEvaluator.class);
    
    private static final String REAL_TOKEN_PREFIX = "FastQT_";
    private static final String REAL_TOKEN_SUFFIX = "QM";
    private static final String EXACT_PREFIX = "exact_";
    private static final String CGI_PATH = "/cgi-bin/xsearch?sources=alone&qtpipeline=lookupword&query=";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    private final HTTPClient httpClient;
    private final Map<String, List<TokenMatch>> analysisResult = new HashMap<String,List<TokenMatch>>();


    /**
     * Search fast and initialize analyzis result.
     * @param query
     */
    VeryFastTokenEvaluator(final HTTPClient client, final String query) {
        // pre-condition check
        if (client == null) {
            throw new IllegalArgumentException("Not allowed to use null HTTPClient!");
        }
        this.httpClient = client;
        queryFast(query);
    }

    /**
     * Find out if given token is on or more of the following.
     *      <li>GEO
     *      <li>FIRSTNAME
     *      <li>LASTNAME
     *      <li>COMPANY
     *      <li>KEYWORDS
     *      <li>CATEGORY
     * </ul>
     *
     * @param token  can be any of the above
     * @return true if the query contains any of the above
     */
    public boolean evaluateToken(final String token, final String term, final String query) {

        boolean evaluation = false;
        final String realTokenFQ = REAL_TOKEN_PREFIX + token + REAL_TOKEN_SUFFIX;

        if (analysisResult.containsKey(realTokenFQ)) {
            if (term == null) {
                evaluation = true;
            }  else  {
                for (TokenMatch occurance : analysisResult.get(realTokenFQ)) {
                    evaluation = occurance.getMatcher(term).find();

                    // keep track of which TokenMatch's we've used.
                    if (evaluation) {
                        occurance.setTouched(true);
                        break;
                    }
                }
            }
        }
        return evaluation;
    }

    public List<TokenMatch> reportToken(final String token, final String query) {

        LOG.trace("reportToken(" + token + "," + query + ")");

        if (evaluateToken(token, null, query)) {
            String realTokenFQ = REAL_TOKEN_PREFIX + token + REAL_TOKEN_SUFFIX;
            return analysisResult.get(realTokenFQ);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Search fast and find out if the given tokesn are company, firstname, lastname etc
     * @param query
     */
    private void queryFast(final String query) {

        LOG.trace("queryFast( " + query + " )");

        if (query == null || query.equals("")) {
            return;
        }

        String url = null;
        try {
            final String token = URLEncoder.encode(query.replaceAll("\"", ""), "utf-8");

            url = CGI_PATH + token;

            final Document doc = httpClient.getXmlDocument("token_evaluator", url);

            NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
            final Element e = (Element) l.item(0);

            l = e.getElementsByTagName("QUERYTRANSFORM");

            for (int i = 0; i < l.getLength(); ++i) {

                final Element trans = (Element) l.item(i);
                final String name = trans.getAttribute("NAME");
                final String custom = trans.getAttribute("CUSTOM");
                final String match = custom.indexOf("->") >0
                        ? custom.substring(0, custom.indexOf("->"))
                        : custom;

                addMatch(name, match, query);

                if (match.equalsIgnoreCase(query.trim())) {

                    final String key = name.substring(name.indexOf('_') + 1, name.indexOf("QM"));

                    addMatch(REAL_TOKEN_PREFIX + EXACT_PREFIX + key + REAL_TOKEN_SUFFIX, match, query);
                }
            }

        } catch (UnsupportedEncodingException ignore) {
            LOG.warn(ERR_FAILED_TO_ENCODE + query);
        } catch (IOException e1) {
            LOG.error("Analysis failed " + url, e1);
        } catch (SAXException e1) {
            LOG.error("XML Parse failure " + url, e1);
        }
    }

    public boolean isQueryDependant(TokenPredicate predicate) {
        return predicate.getFastListName().startsWith(EXACT_PREFIX);
    }

    private void addMatch(final String name, final String match, final String query) {
        final String expr = "\\b" + match + "\\b";
        final Pattern pattern = Pattern.compile(expr, RegExpEvaluatorFactory.REG_EXP_OPTIONS);
        final Matcher m = pattern.matcher(query);

        while (m.find()) {

            final TokenMatch tknMatch = new TokenMatch(name, match, m.start(), m.end());

            if (!analysisResult.containsKey(name)) {
                analysisResult.put(name, new ArrayList());
            }

            analysisResult.get(name).add(tknMatch);
        }
    }
}
