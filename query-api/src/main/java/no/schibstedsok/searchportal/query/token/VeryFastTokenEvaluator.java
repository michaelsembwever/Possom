// Copyright (2005-2007) Schibsted Søk AS
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
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.site.config.DocumentContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.PropertiesContext;

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
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator {

    /** The context required by this class. **/
    public interface Context extends BaseContext, QueryStringContext, DocumentContext, PropertiesContext, SiteContext{
    }

    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(VeryFastTokenEvaluator.class);
    private static final String ERR_FAILED_INITIALISATION = "Failed reading configuration files";
    private static final String ERR_QUERY_FAILED = "Querying the fast list failed on ";
    private static final String ERR_PARSE_FAILED = "XML parsing of fast list response failed on ";
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

    private static final Map<Site,Map<TokenPredicate,String[]>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String[]>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();

    // Attributes ----------------------------------------------------
    
    private volatile boolean init = false;

    private final HTTPClient httpClient;
    private final Context context;
    private final Map<String, List<TokenMatch>> analysisResult = new HashMap<String,List<TokenMatch>>();

    // Static --------------------------------------------------------

    // Constructors -------------------------------------------------
    
    /**
     * Search fast and initialize analysis result.
     * @param query
     */
    VeryFastTokenEvaluator(final Context cxt) throws VeryFastListQueryException{

        // pre-condition check

        context = cxt;

        final Properties props = SiteConfiguration.valueOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class,context)).getProperties();
        final String host = props.getProperty(TOKEN_HOST_PROPERTY);
        final int port = Integer.parseInt(props.getProperty(TOKEN_PORT_PROPERTY));

        httpClient = HTTPClient.instance(host, port);
        
        queryFast(context.getQueryString());
    }

    // Public --------------------------------------------------------
    
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
        final String[] listnames = getFastListNames(token);
        
        if(null != listnames){
            for(int i = 0; !evaluation && i < listnames.length; ++i){

                final String listname = listnames[i];

                if (analysisResult.containsKey(listname)) {
                    if (term == null) {
                        evaluation = true;
                    }  else  {

                        // HACK since DefaultOperatorClause wraps its children in parenthesis
                        final String hackTerm = term.replaceAll("\\(|\\)","");

                        for (TokenMatch occurance : analysisResult.get(listname)) {

                            final Matcher m = occurance.getMatcher(hackTerm);
                            evaluation = m.find() && m.start() == 0 && m.end() == hackTerm.length();

                            // keep track of which TokenMatch's we've used.
                            if (evaluation) {
                                occurance.setTouched(true);
                                break;
                            }
                        }
                    }

                }
            }
        }else{
            LOG.info(context.getSite() + " does not define lists behind the token predicate " + token);
        }
        return evaluation;
    }

    /** TODO comment me. **/
    public boolean isQueryDependant(final TokenPredicate predicate) {
        return predicate.name().startsWith(EXACT_PREFIX.toUpperCase());
    }
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
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
                    },
                    cxt
                ));
        }

        final Site site = cxt.getSite();
        if(LIST_NAMES.get(site) == null){
            
            // create map entry for this site
            LIST_NAMES.put(site, new HashMap<TokenPredicate,String[]>());

            // initialise this site's configuration
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final DocumentLoader loader = cxt.newDocumentLoader(cxt, VERYFAST_EVALUATOR_XMLFILE, builder);
            loader.abut();
            
            LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " started");
            final Map<TokenPredicate,String[]> listNames = LIST_NAMES.get(site);
            final Document doc = loader.getDocument();
            final Element root = doc.getDocumentElement();
            
            if( null != root ){
                final NodeList lists = root.getElementsByTagName("list");
                for (int i = 0; i < lists.getLength(); ++i) {

                    final Element list = (Element) lists.item(i);

                    final String tokenName = list.getAttribute("token");
                    LOG.info(" ->list@token: " + tokenName);

                    final TokenPredicate token = TokenPredicate.valueOf(tokenName);

                    final String[] l = list.getAttribute("list-name").split(",");
                    LOG.info(" ->lists: " + list.getAttribute("list-name"));

                    listNames.put(token, l);

                }
            }
            LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " finished");
        }
    }

    /**
     * Search fast and find out if the given tokens are company, firstname, lastname etc
     * @param query
     */
    private void queryFast(final String query) throws VeryFastListQueryException{

        LOG.trace("queryFast( " + query + " )");

        if (query == null || query.equals("")) {
            return;
        }

        String url = null;
        try {
            final String token = URLEncoder.encode(query.replaceAll("\"", ""), "utf-8");

            url = CGI_PATH + token;

            final Document doc = httpClient.getXmlDocument(url);

            NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
            final Element e = (Element) l.item(0);

            l = e.getElementsByTagName("QUERYTRANSFORM");

            for (int i = 0; i < l.getLength(); ++i) {

                final Element trans = (Element) l.item(i);
                final String name = trans.getAttribute("NAME");
                final String custom = trans.getAttribute("CUSTOM");
                
                if(custom.endsWith("->")){
                    
                    final String match = custom.indexOf("->") >0
                            ? custom.substring(0, custom.indexOf("->"))
                            : custom;

                    addMatch(name, match, query);

                    if (match.equalsIgnoreCase(query.trim())) {

                        final String key = name.substring(name.indexOf('_') + 1, name.indexOf("QM"));

                        addMatch(REAL_TOKEN_PREFIX + EXACT_PREFIX + key + REAL_TOKEN_SUFFIX, match, query);
                    }
                }
            }

        } catch (UnsupportedEncodingException ignore) {
            LOG.warn(ERR_FAILED_TO_ENCODE + query);
        } catch (IOException e1) {
            LOG.error(ERR_QUERY_FAILED + url, e1);
            throw new VeryFastListQueryException(ERR_QUERY_FAILED + url, e1);
        } catch (SAXException e1) {
            LOG.error(ERR_PARSE_FAILED + url, e1);
            throw new VeryFastListQueryException(ERR_PARSE_FAILED + url, e1);
        }
    }

    private void addMatch(final String name, final String match, final String query) {
        
        final String expr = "\\b" + match + "\\b";
        final Pattern pattern = Pattern.compile(expr, RegExpEvaluatorFactory.REG_EXP_OPTIONS);
        final Matcher m = pattern.matcher(query);

        while (m.find()) {

            final TokenMatch tknMatch = new TokenMatch(name, match, m.start(), m.end());
            
            // XXX will store match on every countries different lists supplied in the qm result. Restrict to skin.
            if (!analysisResult.containsKey(name)) {
                analysisResult.put(name, new ArrayList());
            }

            analysisResult.get(name).add(tknMatch);
        }
    }

    private String[] getFastListNames(final TokenPredicate token){
        
        init();
        
        String[] listNames = null;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = context.getSite();
            
            while(null != site){
                
                // find listnames used for this token predicate
                listNames = getFastListNamesImpl(token, site);
                
                // update each listname to the format the fast query matching servers use
                if(null != listNames){
                    for(int i = 0; i < listNames.length; ++i){
                        listNames[i] = REAL_TOKEN_PREFIX + listNames[i] + REAL_TOKEN_SUFFIX;
                    }
                    break;
                }
                
                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return listNames;
    }

    private static String[] getFastListNamesImpl(final TokenPredicate token, final Site site){

        final Map<TokenPredicate,String[]> listNames = LIST_NAMES.get(site);
        return listNames.get(token);
    }
    
    // Inner classes -------------------------------------------------
}
