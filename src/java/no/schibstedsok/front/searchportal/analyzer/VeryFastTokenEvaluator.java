// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.analyzer;

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
import java.util.HashMap;
import java.util.Map;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.front.searchportal.analyzer
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @version $Revision$, $Author$, $Date$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator {

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

        final String realTokenFQ = "FastQT_" + token + "QM";
        return analysisResult.containsKey(realTokenFQ) &&
                ( term == null || analysisResult.get(realTokenFQ).equals(term));
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
                analysisResult.put(name, custom);
                // exact fast analysis hit
                if (custom.equalsIgnoreCase(query)) {
                    final String key = name.substring(name.indexOf('_') + 1, name.indexOf("QM"));
                    analysisResult.put("FastQT_exact_" + key + "QM", custom);
                }
                if (name.indexOf("fullname") > -1) {
                    fullNameMatch = custom;
                }
            }
            // [FIXME] wikiMatch will never be assigned!
            if (wikiMatch != null && fullNameMatch != null) {
                if (fullNameMatch.length() > wikiMatch.length()) {
                    analysisResult.put("FastQT_nameLongerThanWikipediaQM", fullNameMatch);
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
}
