package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.IOException;

import no.schibstedsok.front.searchportal.QueryTokenizer;
import no.schibstedsok.front.searchportal.http.HTTPClient;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.front.searchportal.analyzer
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class VeryFastTokenEvaluator implements TokenEvaluator {

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

    private static Log log = LogFactory.getLog(VeryFastTokenEvaluator.class);
    private Map analysisResult;

    private HTTPClient httpClient = HTTPClient.instance("token_evaluator", "localhost", 15200);

    /**
     * Search fast and initialize analyzis result
     * @param query
     */
    public VeryFastTokenEvaluator(String query) {
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
    public boolean evaluateToken(String token, String query) {

        String realTokenFQ = "FastQT_" + token +"QM";

        return analysisResult.containsKey(realTokenFQ);
    }

    /**
     * Search fast and find out if the given tokesn are company, firstname, lastname etc
     * @param query
     */
    private void queryFast(String query) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: queryFast()");
        }
        analysisResult = new HashMap();

        if (query == null || query.equals("")) {
            return;
        }

        String token = null;
        try {
            token = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException ignore) {
        }

        String url =
                "/cgi-bin/xsearch?hits=10&offset=0&type=all&query=" + token + "&sortby=&rpf_navigation%3Anavigators=&qtpipeline=lookupword&sources=alone";


        try {
            Document doc = httpClient.getXmlDocument("token_evaluator", url);

            NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
            Element e = (Element) l.item(0);

            l = e.getElementsByTagName("QUERYTRANSFORM");

            String wikiMatch = null;
            String fullNameMatch = null;

            for (int i = 0; i < l.getLength();) {
                Element trans = (Element) l.item(i++);
                String name = trans.getAttribute("NAME");
                analysisResult.put(name, new Boolean(true));

                String custom = trans.getAttribute("CUSTOM");

                custom = custom.replaceAll("->", "");

                if (custom.equalsIgnoreCase(query)) {

                    String key = name.substring(name.indexOf('_') + 1, name.indexOf("QM"));

                    analysisResult.put("FastQT_exact_" + key + "QM", new Boolean(true));
                }

                if (name.indexOf("fullname") > -1) {
                    fullNameMatch = custom;
                }
            }

            if (wikiMatch != null && fullNameMatch != null) {
                if (fullNameMatch.length() > wikiMatch.length()) {
                    analysisResult.put("FastQT_nameLongerThanWikipediaQM", new Boolean(true));
                }
            }
        } catch (IOException e1) {
            log.error("Analysis failed " +url, e1);
        } catch (SAXException e1) {
            log.error("XML Parse failure " + url, e1);
        }
    }
}
