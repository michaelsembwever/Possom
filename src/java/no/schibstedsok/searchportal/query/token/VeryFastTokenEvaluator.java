// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.searchportal.query.token;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SiteConfiguration;
import no.schibstedsok.searchportal.util.config.DocumentContext;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.util.config.PropertiesContext;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.util.config.UrlResourceLoader;

import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * VeryFastTokenEvaluator is part of no.schibstedsok.searchportal.query.
 *
 * TODO would make sense to split this class into an Evaluator and a factory, similar to RegExpEvaluator.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @author mick
 * @version $Id$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator, ReportingTokenEvaluator {

    /** The context required by this class. **/
    public interface Context extends BaseContext, QueryStringContext, DocumentContext, PropertiesContext, SiteContext{
    }

    private static final Logger LOG = Logger.getLogger(VeryFastTokenEvaluator.class);
    private static final String ERR_FAILED_INITIALISATION = "Failed reading configuration files";
    private static final String DEBUG_LISTNAME_FOUND_1 = "List for ";
    private static final String DEBUG_LISTNAME_FOUND_2 = " is ";

    /** TODO comment me. **/
    public static final String VERYFAST_EVALUATOR_XMLFILE = "VeryFastEvaluators.xml";
    private static final String TOKEN_HOST_PROPERTY = "tokenevaluator.host";
    private static final String TOKEN_PORT_PROPERTY = "tokenevaluator.port";
    private static final String REAL_TOKEN_PREFIX = "FastQT_";
    private static final String REAL_TOKEN_SUFFIX = "QM";
    private static final String EXACT_PREFIX = "exact_";
    private static final String CGI_PATH = "/cgi-bin/xsearch?sources=alone&qtpipeline=lookupword&query=";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    private static final Map<Site,Map<TokenPredicate,String>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();

    private volatile boolean init = false;

    private final HTTPClient httpClient;
    private final String clientConfId;
    private final Context context;
    private final Map<String, List<TokenMatch>> analysisResult = new HashMap<String,List<TokenMatch>>();


    /**
     * Search fast and initialize analysis result.
     * @param query
     */
    VeryFastTokenEvaluator(final Context cxt){

        // pre-condition check

        context = cxt;

        final Properties props = SiteConfiguration.valueOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class,context)).getProperties();
        final String host = props.getProperty(TOKEN_HOST_PROPERTY);
        final int port = Integer.parseInt(props.getProperty(TOKEN_PORT_PROPERTY));
        clientConfId = "token_evaluator(" + host + ':' + port + ')';

        httpClient = HTTPClient.instance(clientConfId, host, port);
        if (httpClient == null) {
            throw new IllegalArgumentException("Not allowed to use null HTTPClient!");
        }
        queryFast(context.getQueryString());
    }

    private void init() {

        try{
            LIST_NAMES_LOCK.writeLock().lock();

            if (!init) {
                try {
                    initImpl(context);
                } catch (ParserConfigurationException ex) {
                    LOG.error(ERR_FAILED_INITIALISATION, ex);
                }
                init = true;
            }
        }finally{
            LIST_NAMES_LOCK.writeLock().unlock();
        }
    }

    private static void initImpl(final Context cxt) throws ParserConfigurationException  {

        // initialise the parent site's configuration
        final Site parent = cxt.getSite().getParent();
        if(parent != null && LIST_NAMES.get(parent) == null){
            initImpl(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext(){
                        public Site getSite(){
                            return parent;
                        }
                        public PropertiesLoader newPropertiesLoader(final String resource, final Properties props) {
                            return UrlResourceLoader.newPropertiesLoader(this, resource, props);
                        }
                        public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                            return UrlResourceLoader.newDocumentLoader(this, resource, builder);
                        }
                    },
                    cxt
                ));
        }

        final Site site = cxt.getSite();
        if(LIST_NAMES.get(site) == null){
            // create map entry for this site

            LIST_NAMES.put(site, new HashMap<TokenPredicate,String>());

            // initialise this site's configuration
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final DocumentLoader loader = cxt.newDocumentLoader(VERYFAST_EVALUATOR_XMLFILE, builder);
            loader.abut();
            LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " started");
            final Map<TokenPredicate,String> listNames = LIST_NAMES.get(site);
            final Document doc = loader.getDocument();
            final Element root = doc.getDocumentElement();
            final NodeList lists = root.getElementsByTagName("list");
            for (int i = 0; i < lists.getLength(); ++i) {

                final Element list = (Element) lists.item(i);

                final String tokenName = list.getAttribute("token");
                LOG.info(" ->list@token: " + tokenName);

                final TokenPredicate token = TokenPredicate.valueOf(tokenName);

                final String listName = list.getAttribute("list-name");
                LOG.info(" ->list: " + listName);

                listNames.put(token, listName);

            }
            LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " finished");
        }
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
                
                // HACK since DefaultOperatorClause wraps its children in parenthesis 
                final String hackTerm = term.replaceAll("\\(|\\)","");
                
                for (TokenMatch occurance : analysisResult.get(realTokenFQ)) {

                    final Matcher m =occurance.getMatcher(hackTerm);
                    evaluation = m.find() && m.start() == 0 && m.end() == hackTerm.length();

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

    /** TODO comment me. **/
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

            final Document doc = httpClient.getXmlDocument(clientConfId, url);

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

    /** TODO comment me. **/
    public boolean isQueryDependant(final TokenPredicate predicate) {
        return predicate.name().startsWith(EXACT_PREFIX.toUpperCase());
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
        Site site = context.getSite();
        String listName = null;
        while(listName == null && site != null){
            listName = getFastListNameImpl(token, site);
            site = site.getParent();
        }
        LIST_NAMES_LOCK.readLock().unlock();
        return REAL_TOKEN_PREFIX + listName + REAL_TOKEN_SUFFIX;
    }

    private static String getFastListNameImpl(final TokenPredicate token, final Site site){

        final Map<TokenPredicate,String> listNames = LIST_NAMES.get(site);
        return listNames.get(token);
    }
}
