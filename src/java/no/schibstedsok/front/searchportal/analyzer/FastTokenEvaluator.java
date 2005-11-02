package no.schibstedsok.front.searchportal.analyzer;

import no.schibstedsok.front.searchportal.QueryTokenizer;

import java.util.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.StopWatch;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastTokenEvaluator implements TokenEvaluator {

    private static Log log = LogFactory.getLog(FastTokenEvaluator.class);
    private static HttpConnectionManager cManager = new MultiThreadedHttpConnectionManager();
    private Map analysisResult;
    private static List knownTokens = new ArrayList();
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    static {
        knownTokens.add("geo");
        knownTokens.add("firstname");
        knownTokens.add("lastname");
        knownTokens.add("company");
        knownTokens.add("keywords");
        knownTokens.add("category");
    }

    public FastTokenEvaluator(String query) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: FastTokenEvaluator()");
        }

        StopWatch watch = new StopWatch();

        watch.start();
        queryFast(query);
        watch.stop();

        if (log.isDebugEnabled()) {
            log.debug("Fast text analysis took " + watch);
        }
    }


    public boolean evaluateToken(String token, String query) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: evaluateToken()");
        }
        return analysisResult.containsKey(token);
    }

    public static void main(String[] args) {
        FastTokenEvaluator te = new FastTokenEvaluator("lkjhsdlkjfhsdkf");
    }


    private void queryFast(String query) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: queryFast()");
        }

        analysisResult = new HashMap();

        DocumentBuilder builder = null;
        try {
            StopWatch watch = null;

            if (log.isDebugEnabled()) {
                watch = new StopWatch();
                watch.start();
            }

            builder = factory.newDocumentBuilder();

            if (log.isDebugEnabled()) {
                watch.stop();
                log.debug("Created a new document builder in " + watch);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        List tokens = QueryTokenizer.tokenize(query);
        HttpClient client = new HttpClient(cManager);

        for (Iterator iterator = tokens.iterator(); iterator.hasNext();) {
            String token = (String) iterator.next();

            String encodedToken = null;
            try {
                encodedToken = URLEncoder.encode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            for (Iterator iterator1 = knownTokens.iterator(); iterator1.hasNext();) {
                String knownToken = (String) iterator1.next();
                String url = "http://localhost:15200/cgi-bin/xsearch?query=" + encodedToken + "&qtpipeline=lookup" + knownToken + "&sources=alone";

                GetMethod get = new GetMethod(url);

                try {
                    if(log.isDebugEnabled()){
                        log.debug("queryFast: executeMethod");
                    }

                    if(log.isInfoEnabled()){
                        log.info("queryFast(): Fast Search: " + url);
                    }
                    client.executeMethod(get);
                    String response = get.getResponseBodyAsString();

                    if(log.isDebugEnabled()){
                        log.debug("queryFast(): Parsing response ");
                    }
                    Document doc = builder.parse(new InputSource(new StringReader(response)));
                    NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
                    Element e = (Element) l.item(0);
                    l = e.getElementsByTagName("QUERYTRANSFORM");

                    for (int i = 0; i < l.getLength();) {
                        Element trans = (Element) l.item(i++);
                        String name = trans.getAttribute("NAME");

                        if (name.equals("FastQT_DidYouMean")) {
                            String q = trans.getAttribute("QUERY");
                            q= q.replaceAll("\"", "");

                            if(log.isDebugEnabled()){
                                log.debug("queryFast: " + q + ".equals(" + token +") ? " +
                                        (q.equalsIgnoreCase(token) ? " YES " : " NO "));
                            }
                            if (q.equalsIgnoreCase(token)) {
                                if (!analysisResult.containsKey(knownToken)) {
                                    analysisResult.put(knownToken, "yes");
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("queryFast", e);
                } catch (SAXException e) {
                    log.error("queryFast", e);
                }

            }
        }
        long end = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Fast textual analysis took " + (end - start));
        }
    }

}
