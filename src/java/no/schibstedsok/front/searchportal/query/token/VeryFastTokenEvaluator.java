// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.front.searchportal.analyzer
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @version $Revision$, $Author$, $Date$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator, ReportingTokenEvaluator {

    /* Geo data*/
    public static final String GEO = "geo";
    /* String representing Firstname */
    public static final String FIRSTNAME = "firstname";
    /* String representing Lastname */
    public static final String LASTNAME = "lastname";
    /* String representing Company */
    public static final String COMPANY = "companyname";
    /* String representing Keywords */
    public static final String KEYWORDS = "keywords";
    /* String representing Category */
    public static final String CATEGORY = "category";

    private static final Log LOG = LogFactory.getLog(VeryFastTokenEvaluator.class);
    private final Map/*<String>,<String>*/ analysisResult = new HashMap/*<String>,<String>*/();

    private HTTPClient httpClient = null;
    private Locale locale;

    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    /**
     * Search fast and initialize analyzis result
     * @param query
     */
    public VeryFastTokenEvaluator(final HTTPClient client, final String query) {
        // pre-condition check
        if ( client == null ) {
            throw new IllegalArgumentException("Not allowed to use null HTTPClient!");
        }
        this.httpClient = client;
        queryFast(query);
    }

    /**
     * Find out if given token is on or more of the following:
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
        final String realTokenFQ = "FastQT_" + token + "QM";
        
        if( analysisResult.containsKey(realTokenFQ) ){
            if( term == null ){
                evaluation = true;
            }else{
                final List/*<TokenMatch>*/ occurances = (List/*<TokenMatch>*/)analysisResult.get(realTokenFQ);
                for( Iterator it = occurances.iterator(); !evaluation && it.hasNext(); ){
                    final TokenMatch occurance = (TokenMatch)it.next();
                    evaluation = occurance.getMatch().equals(term);
                }
            }
        }
        return evaluation;
    }

    public List reportToken(String token, String query) {
        LOG.debug("SALE called");
        if (evaluateToken(token, null, query)) {
            String realTokenFQ = "FastQT_" + token +"QM";
            List matches = (List) analysisResult.get(realTokenFQ);
            return matches;
        } else {
            return new ArrayList();
        }
    }

    /**
     * Search fast and find out if the given tokesn are company, firstname, lastname etc
     * @param query
     */
    private void queryFast(final String query) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: queryFast( " + query + " )");
        }

        if (query == null || query.equals("")) {
            return;
        }

        String token = null;
        try {
            token = URLEncoder.encode(query.replaceAll("\"", ""), "utf-8");
        } catch (UnsupportedEncodingException ignore) {
            LOG.warn(ERR_FAILED_TO_ENCODE + query);
        }

        final String url = "/cgi-bin/xsearch?hits=10&offset=0&type=all&query="
                + token + "&sortby=&rpf_navigation%3Anavigators=&qtpipeline=lookupword&sources=alone";

        try {

            final Document doc = httpClient.getXmlDocument("token_evaluator", url);


            NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
            final Element e = (Element) l.item(0);

            l = e.getElementsByTagName("QUERYTRANSFORM");

            String wikiMatch = null;
            String fullNameMatch = null;

            for (int i = 0; i < l.getLength(); ++i) {

                final Element trans = (Element) l.item(i);
                final String name = trans.getAttribute("NAME");
                final String custom = trans.getAttribute("CUSTOM").replaceAll("->", "");
                // basic fast analysis hit

                addMatch(name, custom, query);
                

                if (custom.equalsIgnoreCase(query)) {

                    final String key = name.substring(name.indexOf('_') + 1, name.indexOf("QM"));

                    addMatch("FastQT_exact_" + key + "QM", custom, query);
                }
                if (name.indexOf("fullname") > -1) {
                    fullNameMatch = custom;
                }
            }
            // [FIXME] wikiMatch will never be assigned!
            if (wikiMatch != null && fullNameMatch != null) {
                if (fullNameMatch.length() > wikiMatch.length()) {
                    analysisResult.put("FastQT_nameLongerThanWikipediaQM", fullNameMatch);
                    addMatch("FastQT_nameLongerThanWikipediaQM", fullNameMatch, query);

                }
            }
        } catch (IOException e1) {
            LOG.error("Analysis failed " + url, e1);
        } catch (SAXException e1) {
            LOG.error("XML Parse failure " + url, e1);
        }
    }

    public boolean isQueryDependant() {
        return false;
    }

    private void addMatch(final String name, final String custom, final String query) {
        final String expr = "\\b" + custom + "\\b";
        final Pattern pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        final Matcher m = pattern.matcher(query);

        while (m.find()) {
            
            final TokenMatch match = new TokenMatch(name, custom, m.start(), m.end());

            if (!analysisResult.containsKey(name)) {
                final List matches = new ArrayList();
                analysisResult.put(name, matches);
            }
            
            final List previousMatches = (List) analysisResult.get(name);
            previousMatches.add(match);
        }
    }

}
