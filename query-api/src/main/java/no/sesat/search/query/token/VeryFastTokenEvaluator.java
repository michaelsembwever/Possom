/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.token;


import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.DocumentContext;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.PropertiesContext;

import no.sesat.search.http.HTTPClient;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.query.parser.QueryParser;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;


import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * VeryFastTokenEvaluator is part of no.sesat.search.query.
 *
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @author <a href="mailto:mick@wever.org">Mck Semb Wever</a>
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

    /** The configuration file from the skin that specifies token predicate to list mappings. **/
    public static final String VERYFAST_EVALUATOR_XMLFILE = "VeryFastEvaluators.xml";
    private static final String TOKEN_HOST_PROPERTY = "tokenevaluator.host";
    private static final String TOKEN_PORT_PROPERTY = "tokenevaluator.port";
    private static final String LIST_PREFIX = "FastQT_";
    private static final String LIST_SUFFIX = "QM";
    private static final String EXACT_PREFIX = "exact_";
    private static final String CGI_PATH = "/cgi-bin/xsearch?sources=alone&qtpipeline=lookupword&query=";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    private static final Map<Site,Map<TokenPredicate,String[]>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String[]>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();
    
    private static final GeneralCacheAdministrator CACHE = new GeneralCacheAdministrator();   
    private static final int REFRESH_PERIOD = 60; // one minute
    private static final int CACHE_CAPACITY = 1000;
    
    private static final String SKIP_REGEX;
    private static final String OPERATOR_REGEX;

    // Attributes ----------------------------------------------------
    
    private final HTTPClient httpClient;
    private final Context context;
    private final Map<String, List<TokenMatch>> analysisResult;

    // Static --------------------------------------------------------
    
    static{
        CACHE.setCacheCapacity(CACHE_CAPACITY);
        
        // build our skip regular expression
        final StringBuilder builder = new StringBuilder();
        for(char[] range : QueryParser.SKIP_CHARACTER_RANGES){
            builder.append("[\\" + range[0] + "-\\" + range[1] + "]|");
        }
        // remove trailing '|'
        builder.setLength(builder.length() - 1);
        // our skip regular expression
        SKIP_REGEX = '(' + builder.toString() + ')';

        final StringBuilder operatorRegexpBuilder = new StringBuilder();

        operatorRegexpBuilder.append("[");

        for (char c : QueryParser.OPERATOR_CHARACTERS) {
            operatorRegexpBuilder.append('\\').append(c);
        }

        operatorRegexpBuilder.append("]");

        OPERATOR_REGEX = operatorRegexpBuilder.toString();
    }

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
        
        init();

        // Remove whitespace (except space itself) and operator characters.
        analysisResult = queryFast(context.getQueryString()
                .replaceAll(" ", "xxKEEPWSxx") // Hack to keep spaces.
                .replaceAll(SKIP_REGEX, "")
                .replaceAll(OPERATOR_REGEX, "")
                .replaceAll("xxKEEPWSxx", " ")); // Hack to keep spaces.
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
        final String[] listnames = getListNames(token);
        
        if(null != listnames){
            for(int i = 0; !evaluation && i < listnames.length; ++i){

                final String listname = listnames[i];

                if (analysisResult.containsKey(listname)) {
                    if (term == null) {
                        evaluation = true;
                    }  else  {

                        // HACK since DefaultOperatorClause wraps its children in parenthesis
                        // Also remove any operator characters. (SEARCH-3883)
                        final String hackTerm = term.replaceAll("\\(|\\)","").replaceAll(OPERATOR_REGEX, "");

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

    /** 
     * 
     * @param predicate
     * @return
     */
    public boolean isQueryDependant(final TokenPredicate predicate) {
        return predicate.name().startsWith(EXACT_PREFIX.toUpperCase());
    }
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private void init() {

        try {
            initImpl(context);
        } catch (ParserConfigurationException ex) {
            LOG.error(ERR_FAILED_INITIALISATION, ex);
        }
    }

    private static void initImpl(final Context cxt) throws ParserConfigurationException  {

        final Site site = cxt.getSite();
        final Site parent = site.getParent();
        final boolean parentUninitialised;
        
        try{
            LIST_NAMES_LOCK.readLock().lock();
            
            // initialise the parent site's configuration
            parentUninitialised = (null != parent && null == LIST_NAMES.get(parent));
            
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        
        if(parentUninitialised){
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
        
        try{
            LIST_NAMES_LOCK.writeLock().lock();
        
            if(null == LIST_NAMES.get(site)){

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

                        final String[] listNameArr = list.getAttribute("list-name").split(",");
                        LOG.info(" ->lists: " + list.getAttribute("list-name"));

                        // update each listname to the format the fast query matching servers use
                        if(null != listNameArr){
                            for(int j = 0; j < listNameArr.length; ++j){
                                listNameArr[j] = LIST_PREFIX + listNameArr[j] + LIST_SUFFIX;
                            }

                            // put the listnames in
                            Arrays.sort(listNameArr, null);
                            listNames.put(token, listNameArr);
                        }


                    }
                }
                LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " finished");
            }
        }finally{
            LIST_NAMES_LOCK.writeLock().unlock();
        }
    }

    /**
     * Search fast and find out if the given tokens are company, firstname, lastname etc
     * @param query
     */
    private Map<String, List<TokenMatch>> queryFast(final String query) throws VeryFastListQueryException{

        LOG.trace("queryFast( " + query + " )");
        Map<String, List<TokenMatch>> result = null;

        if (query != null && 0 < query.length()) {

            try{
                result = (Map<String, List<TokenMatch>>) CACHE.getFromCache(query, REFRESH_PERIOD);

            } catch (NeedsRefreshException nre) {
            
                boolean updatedCache = false;
                result = new HashMap<String,List<TokenMatch>>();
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
                        final String exactname = 0 <= name.indexOf(LIST_PREFIX) && 0 < name.indexOf(LIST_SUFFIX)
                                ? LIST_PREFIX + EXACT_PREFIX 
                                    + name.substring(name.indexOf('_') + 1, name.indexOf("QM"))
                                    + LIST_SUFFIX
                                : null;

                        if(custom.endsWith("->") && usesListName(name, exactname)){

                            final String match = (custom.indexOf("->") >0
                                    ? custom.substring(0, custom.indexOf("->"))
                                    : custom)
                                    // remove words made solely of characters that the parser considers whitespace
                                    .replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");

                            addMatch(name, match, query, result);

                            if (match.equalsIgnoreCase(query.trim())) {

                                addMatch(exactname, match, query, result);
                            }
                        }
                    }
                    CACHE.putInCache(query, result);
                    updatedCache = true;

                } catch (UnsupportedEncodingException ignore) {
                    LOG.warn(ERR_FAILED_TO_ENCODE + query);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                } catch (IOException e1) {
                    LOG.error(ERR_QUERY_FAILED + url, e1);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                    throw new VeryFastListQueryException(ERR_QUERY_FAILED + url, e1);
                } catch (SAXException e1) {
                    LOG.error(ERR_PARSE_FAILED + url, e1);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                    throw new VeryFastListQueryException(ERR_PARSE_FAILED + url, e1);
                }finally{
                    if(!updatedCache){ 
                        CACHE.cancelUpdate(query);
                    }
                }
            }
        } else {
            result = Collections.emptyMap();
        }
        return result;
    }

    private static void addMatch(
            final String name, 
            final String match, 
            final String query,
            final Map<String, List<TokenMatch>> result) {
        
        final String expr = "\\b" + match + "\\b";
        final Pattern pattern = Pattern.compile(expr, RegExpEvaluatorFactory.REG_EXP_OPTIONS);
        final String qNew = query.replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");
        final Matcher m = pattern.matcher(
                // remove words made solely of characters that the parser considers whitespace
                qNew);

        while (m.find()) {

            final TokenMatch tknMatch = new TokenMatch(name, match, m.start(), m.end());
            
            if (!result.containsKey(name)) {
                result.put(name, new ArrayList<TokenMatch>());
            }

            result.get(name).add(tknMatch);

            if (result.get(name).size() % 100 == 0) {
                LOG.warn("Pattern: " + pattern.pattern() + " name: " + name + " query: " + query + " match: " + match + " query2: " + qNew);
            }
        }
    }

    private boolean usesListName(final String listname, final String exactname){
               
        boolean uses = false;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = context.getSite();
            
            while(!uses && null != site){
                
                // find listnames used for this token predicate
                for(String[] listnames : LIST_NAMES.get(site).values()){
                    uses |= 0 <= Arrays.binarySearch(listnames, listname, null);
                    uses |= null != exactname && 0 <= Arrays.binarySearch(listnames, exactname, null);
                    if(uses){  break; }
                }
                                
                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return uses;
    }
    private String[] getListNames(final TokenPredicate token){
        
        
        
        String[] listNames = null;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = context.getSite();
            
            while(null == listNames && null != site){
                
                // find listnames used for this token predicate
                listNames = LIST_NAMES.get(site).get(token);
                                
                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return listNames;
    }
    
    // Inner classes -------------------------------------------------
}
