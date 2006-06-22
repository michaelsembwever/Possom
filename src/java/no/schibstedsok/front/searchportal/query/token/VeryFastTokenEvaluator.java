// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentContext;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;

import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.front.searchportal.query.
 *
 * TODO would make sense to split this class into an Evaluator and a factory, similar to RegExpEvaluator.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @author mick
 * @version $Id$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator, ReportingTokenEvaluator {

    public interface Context extends BaseContext, QueryStringContext, DocumentContext, SiteContext{
    }

    private static final Logger LOG = Logger.getLogger(VeryFastTokenEvaluator.class);

    public static String VERYFAST_EVALUATOR_XMLFILE = "VeryFastEvaluators.xml";
    private static final String REAL_TOKEN_PREFIX = "FastQT_";
    private static final String REAL_TOKEN_SUFFIX = "QM";
    private static final String EXACT_PREFIX = "exact_";
    private static final String CGI_PATH = "/cgi-bin/xsearch?sources=alone&qtpipeline=lookupword&query=";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    private static final Map<Site,Map<TokenPredicate,String>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();
    private final DocumentLoader loader;
    private volatile boolean init = true;

    private final HTTPClient httpClient;
    private final Context context;
    private final Map<String, List<TokenMatch>> analysisResult = new HashMap<String,List<TokenMatch>>();


    /**
     * Search fast and initialize analysis result.
     * @param query
     */
    VeryFastTokenEvaluator(final HTTPClient client, final Context cxt)
            throws ParserConfigurationException  {

        // pre-condition check
        if (client == null) {
            throw new IllegalArgumentException("Not allowed to use null HTTPClient!");
        }
        this.httpClient = client;
        LIST_NAMES_LOCK.writeLock().lock();
        context = cxt;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        loader = context.newDocumentLoader(VERYFAST_EVALUATOR_XMLFILE, builder);

        if(LIST_NAMES.get(context.getSite()) == null){
            LIST_NAMES.put(context.getSite(), new HashMap<TokenPredicate,String>());
            init = false;
        }

        LIST_NAMES_LOCK.writeLock().unlock();
        queryFast(context.getQueryString());
    }

    private void init() {

        LIST_NAMES_LOCK.writeLock().lock();
        if (!init) {
            loader.abut();
            LOG.debug("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " started");
            final Map<TokenPredicate,String> listNames = LIST_NAMES.get(context.getSite());
            final Document doc = loader.getDocument();
            final Element root = doc.getDocumentElement();
            final NodeList lists = root.getElementsByTagName("list");
            for (int i = 0; i < lists.getLength(); ++i) {

                final Element list = (Element) lists.item(i);

                final String tokenName = list.getAttribute("token");
                LOG.debug(" ->list@token: " + tokenName);

                final TokenPredicate token = TokenPredicate.valueOf(tokenName);

                final String listName = list.getAttribute("list-name");
                LOG.debug(" ->list: " + listName);

                listNames.put(token, listName);

            }
            LOG.debug("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " finished");
            init = true;
        }

        LIST_NAMES_LOCK.writeLock().unlock();
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
    public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {

        boolean evaluation = false;
        final String realTokenFQ = getFastListName(token);

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

    public List<TokenMatch> reportToken(final TokenPredicate token, final String query) {

        LOG.trace("reportToken(" + token + "," + query + ")");

        if (evaluateToken(token, null, query)) {
            final String realTokenFQ = getFastListName(token);
            return analysisResult.get(realTokenFQ);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Search fast and find out if the given tokens are company, firstname, lastname etc
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

    public boolean isQueryDependant(final TokenPredicate predicate) {
        return predicate.name().startsWith(EXACT_PREFIX);
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

    private String getFastListName(final TokenPredicate token){
        init();
        LIST_NAMES_LOCK.readLock().lock();
        final Map<TokenPredicate,String> listNames = LIST_NAMES.get(context.getSite());
        final String listName = listNames.get(token);
        LIST_NAMES_LOCK.readLock().unlock();
        return REAL_TOKEN_PREFIX + listName + REAL_TOKEN_SUFFIX;
    }
}
