/* Copyright (2005-2008) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.token;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.sesat.search.query.token.AbstractEvaluatorFactory.Context;
import org.apache.log4j.Level;
import static no.sesat.search.query.parser.AbstractQueryParser.SKIP_REGEX;
import static no.sesat.search.query.parser.AbstractQueryParser.OPERATOR_REGEX;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @version $Id$
 */
public final class SolrTokenEvaluator implements TokenEvaluator{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SolrTokenEvaluator.class);
    private static final Logger DUMP = Logger.getLogger("no.sesat.search.Dump");

    /** General properties to regular expressions configured. **/
    private static final int REG_EXP_OPTIONS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    // use the lowercase version of TokenPredicate.EXACT_PREFIX
    private static final String EXACT_PREFIX = TokenPredicate.EXACT_PREFIX.toLowerCase();


    private static final GeneralCacheAdministrator CACHE_QUERY = new GeneralCacheAdministrator();
    private static final int REFRESH_PERIOD = 60;
    // smaller than usual as each entry can contain up to 600 values!
    private static final int CACHE_QUERY_CAPACITY = 100;
    private static final int INITIAL_ROWS_TO_FETCH = 25;

    private static final String ERR_QUERY_FAILED = "Querying Solr failed on ";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    // Attributes ----------------------------------------------------

    private final Context context;
    private SolrEvaluatorFactory factory;
    private final Map<String, List<TokenMatch>> analysisResult;

    // Static --------------------------------------------------------

    static{
        CACHE_QUERY.setCacheCapacity(CACHE_QUERY_CAPACITY);
    }

    // Constructors --------------------------------------------------

    /** Only possible constructor.
     *
     * @param cxt the required context
     * @param factory the factory constructing this.
     * @throws EvaluationException if the evaluation, request to the solr index, fails.
     */
    SolrTokenEvaluator(final Context cxt, final SolrEvaluatorFactory factory) throws EvaluationException{

        context = cxt;
        this.factory = factory;

        // the responsible() method checks if we are responsible for any tokenPredicate at all.
        // if we are not then the restful request in query(..) is a waste of time and resource.
        analysisResult = factory.responsible()
                ? query(cleanString(context.getQueryString()))
                : Collections.<String, List<TokenMatch>>emptyMap();
    }

    // Public --------------------------------------------------------


    public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {

        boolean evaluation = false;

        if(!analysisResult.isEmpty()){

            final String[] listnames = factory.getListNames(token);

            if(null != listnames){
                for(int i = 0; !evaluation && i < listnames.length; ++i){

                    final String listname = listnames[i];

                    if (analysisResult.containsKey(listname)) {
                        if (term == null) {
                            evaluation = true;
                        }  else  {

                            // HACK since DefaultOperatorClause wraps its children in parenthesis
                            final String hackTerm = cleanString(term.replaceAll("\\(|\\)",""));

                            for (TokenMatch occurance : analysisResult.get(listname)) {

                                final Matcher m = occurance.getMatcher(hackTerm);
                                evaluation = m.find() && m.start() == 0 && m.end() == hackTerm.length();

                                if (evaluation) {
                                    break;
                                }
                            }
                        }

                    }
                }
            }else{
                LOG.info(context.getSite() + " does not define lists behind the token predicate " + token);
            }
        }
        return evaluation;
    }


    /**
     * get all match values and values for given list .
     *
     * @param token
     * @param term
     * @return a list of Tokens
     */
    public Set<String> getMatchValues(final TokenPredicate token, final String term) {

        final Set<String> values;

        if(!analysisResult.isEmpty()){

            values = new HashSet<String>();

            final String[] listnames = factory.getListNames(token);
            if(null != listnames){
                for(int i = 0; i < listnames.length; i++){
                    final String listname = listnames[i];

                    if (analysisResult.containsKey(listname)) {

                        // HACK since DefaultOperatorClause wraps its children in parenthesis
                        final String hackTerm = cleanString(term.replaceAll("\\(|\\)",""));

                        for (TokenMatch occurance : analysisResult.get(listname)) {

                            final Matcher m = occurance.getMatcher(hackTerm);

                            if (m.find() && m.start() == 0 && m.end() == hackTerm.length()) {
                                values.add(occurance.getValue());
                            }
                        }
                    }
                }
            }
        }else{
            values = Collections.<String>emptySet();
        }
        return Collections.unmodifiableSet(values);
    }


    public boolean isQueryDependant(TokenPredicate predicate) {

        return predicate.name().startsWith(EXACT_PREFIX.toUpperCase());
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    /**
     * Search solr and find out if the given tokens are company, firstname, lastname etc
     * @param query
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<TokenMatch>> query(final String query) throws EvaluationException{

        LOG.trace("queryFast( " + query + " )");
        Map<String, List<TokenMatch>> result = null;

        if (query != null && 0 < query.length()) {

            try{
                result = (Map<String, List<TokenMatch>>) CACHE_QUERY.getFromCache(query, REFRESH_PERIOD);

            } catch (NeedsRefreshException nre) {

                boolean updatedCache = false;
                result = new HashMap<String,List<TokenMatch>>();
                String url = null;

                try {
                    final String token = query.replaceAll("\"", "");

                    // set up query
                    final SolrQuery solrQuery = new SolrQuery()
                            .setQuery("list_entry_shingle:\"" + token + "\"")
                            .setRows(INITIAL_ROWS_TO_FETCH);

                    // when the root logger is set to DEBUG do not limit connection times
                    if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
                        // default timeout is half second. TODO make configuration.
                        solrQuery.setTimeAllowed(500);
                    }

                    // query for hits
                    QueryResponse response = factory.getSolrServer().query(solrQuery);
                    final int numberOfHits = (int)response.getResults().getNumFound();

                    boolean more = false;
                    do {
                        DUMP.info(solrQuery.toString());

                        final SolrDocumentList docs = response.getResults();

                        // iterate through docs
                        for(SolrDocument doc : docs){

                            final String name = (String) doc.getFieldValue("list_name");
                            final String exactname = EXACT_PREFIX + name;

                            // remove words made solely of characters that the parser considers whitespace
                            final String hit = ((String) doc.getFieldValue("list_entry"))
                                    .replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");

                            final String synonym = (String) doc.getFieldValue("list_entry_synonym");

                            if(factory.usesListName(name, exactname)){

                                addMatch(name, hit, synonym, query, result);

                                if (hit.equalsIgnoreCase(query.trim())) {

                                    addMatch(exactname, hit, synonym, query, result);
                                }
                            }
                        }

                        int rest = numberOfHits - INITIAL_ROWS_TO_FETCH;
                        if (!more && rest > 0) {
                            more = true;
                            solrQuery.setStart(INITIAL_ROWS_TO_FETCH);
                            solrQuery.setRows(rest);
                            // query
                            response = factory.getSolrServer().query(solrQuery);
                        }else {
                            more = false;
                        }
                    }while (more);


                    result = Collections.<String,List<TokenMatch>>unmodifiableMap(result);
                    CACHE_QUERY.putInCache(query, result);
                    updatedCache = true;

                } catch (SolrServerException ex) {
                    LOG.error(ex.getMessage(), ex);
                    throw new EvaluationException(ERR_QUERY_FAILED + url, ex);

                }finally{
                    if(!updatedCache){
                        CACHE_QUERY.cancelUpdate(query);
                    }
                }
            }
        } else {
            result = Collections.<String, List<TokenMatch>>emptyMap();
        }
        return result;
    }

    private static void addMatch(
            final String name,
            final String match,
            final String value,
            final String query,
            final Map<String, List<TokenMatch>> result) {

        final String expr = "\\b" + match + "\\b";
        final Pattern pattern = Pattern.compile(expr, REG_EXP_OPTIONS);
        final String qNew = query.replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");
        final Matcher m = pattern.matcher(
                // remove words made solely of characters that the parser considers whitespace
                qNew);

        while (m.find()) {
            final TokenMatch tknMatch = TokenMatch.instanceOf(name, match, value);

            if (!result.containsKey(name)) {
                result.put(name, new ArrayList<TokenMatch>());
            }

            result.get(name).add(tknMatch);

            if (result.get(name).size() % 100 == 0) {
                LOG.warn("Pattern: " + pattern.pattern()
                        + " name: " + name
                        + " query: " + query
                        + " match: " + match
                        + " query2: " + qNew);
            }
        }
    }

    private String cleanString(final String string){

        // Strip out SKIP characters we are not interested in.
        // Also remove any operator characters. (SEARCH-3883 & SEARCH-3967)

        return string.toLowerCase()
                .replaceAll(" ", "xxKEEPWSxx") // Hack to keep spaces. multiple spaces always normalised.
                .replaceAll(SKIP_REGEX, " ")
                .replaceAll("xxKEEPWSxx", " ") // Hack to keep spaces.
                .replaceAll(OPERATOR_REGEX, " ")
                .replaceAll(" +", " "); // normalise
    }


    // Inner classes -------------------------------------------------
}
