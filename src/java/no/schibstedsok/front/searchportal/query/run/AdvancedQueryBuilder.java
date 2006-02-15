package no.schibstedsok.front.searchportal.query.run;

import no.schibstedsok.front.searchportal.query.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * AdvancedQueryBuilder is part of no.schibstedsok.front.searchportal.query
 * It will insert Fast specific operators AND, NOT, OR, ANDNOT and so forth
 * to build up an advanced querystring.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola@schibstedsok.no</a>
 * @version 0.1
 * @version $Revision$, $Author$, $Date$
 */
public class AdvancedQueryBuilder {


    static String regexAdvancedQuery = ".*( AND | OR | NOT | ANDNOT ).*";
    static Pattern pattern = Pattern.compile(regexAdvancedQuery);

    Matcher matcher;

    Log log = LogFactory.getLog(AdvancedQueryBuilder.class);

    private String queryAllWords;
    private String queryAnyWords;
    private String queryPhrase;
    private String queryNot;
    private String query;
    private String filterLanguage;

    /**
     * New instance set query using properiate set methods
     */
    public AdvancedQueryBuilder(){}

    /**
     * Create new instance of query builder with an advanced search.
     * QueryBuilder will build a valid query by inserting operators
     * like AND, NOT, ANDNOT and OR
     *
     * @param qAll     match all words
     * @param qPhrase  match excact phrase
     * @param qOr      match one of these words
     * @param qNot     don't match any of these words
     */
    public AdvancedQueryBuilder(String qAll,
                                String qPhrase, 
                                String qOr,
                                String qNot){
        queryAllWords = qAll;
        queryPhrase = qPhrase;
        queryAnyWords = qOr;
        queryNot = qNot;
    }

    /**
     * Create a new Instance of QueryBuilder parsing the request.
     * The following request parameters will be used to build an
     * advanced query
     * <ul>
     *      <li> q_all  Match all words
     *      <li> q_phrase Match phrase
     *      <li> q_or   Match any words
     *      <li> q_not  Not these words
     *      <li> q_lang Language
     * </ul>
     * @param request
     */
    public AdvancedQueryBuilder(HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("ENTR: AdvancedQueryBuilder(request)");
        }
        queryAllWords = request.getParameter("q_all");
        queryPhrase = request.getParameter("q_phrase");
        queryAnyWords = request.getParameter("q_or");
        queryNot = request.getParameter("q_not");
        setFilterLanguage(request.getParameter("q_lang"));
    }

    /**
     * Test if a given query has keyords that indicates that
     * the query is of type advanced
     * @param query search
     * @return true if the query is advanced
     */
    public static boolean isAdvancedQuery(String query){
        return  pattern.matcher(query).matches();
    }

    /**
     * As of now, this function returns "adv" for advanced. But it may
     * also be smarter and return "any", "phrase", if that is what
     * the search actually is.
     *
     * @return search type adv
     */
    public String getType(){
        return "adv" ;
    }

    /**
     * Return the filter type, if language is set then the filtertype is
     * "adv" for advanced. Otherwise defaulting to "any"
     * @return
     */
    public String getFilterType(){
        if(filterLanguage == null){
            return "any";
        }else {
            return "adv";
        }
    }

    /**
     * Get advanced query..
     * @return advanced query with operators
     */
    public String getQuery(){
        if( query == null || queryAllWords != null || queryAnyWords != null || queryNot != null || queryPhrase != null ){
            createQuery();
        }
        return query;
    }

    // Getters and setters
    public String getFilterLanguage() {
        return filterLanguage;
    }

    public void setFilterLanguage(String filterLanguage) {
        this.filterLanguage = "language:" + filterLanguage;
    }

    public String getQueryAllWords() {
        return queryAllWords;
    }

    public void setQueryAllWords(String queryAllWords) {
        this.queryAllWords = queryAllWords;
    }

    public String getQueryAnyWords() {
        return queryAnyWords;
    }

    public void setQueryAnyWords(String queryAnyWords) {
        this.queryAnyWords = queryAnyWords;
    }

    public String getQueryPhrase() {
        return queryPhrase;
    }

    public void setQueryPhrase(String queryPhrase) {
        this.queryPhrase = queryPhrase;
    }

    public String getQueryNot() {
        return queryNot;
    }

    public void setQueryNot(String queryNot) {
        this.queryNot = queryNot;
    }

    /*
     * create query
     */
    private void createQuery(){

        StringBuffer query= this.query == null ? new StringBuffer() : new StringBuffer(this.query);
        myAppender(query, queryAllWords, "AND", false, null);
        myAppender(query, queryAnyWords, "OR", query.length() != 0, "OR");
        query.append(insertPhrase(queryPhrase, query.length() != 0));
        myAppender(query, queryNot, "ANDNOT", true, "NOT"); // if query.length() == null prefix must become "NOT"
        
        // we now have a query built
        this.query = query.toString();
        // reset query components
        queryAllWords = queryAnyWords = queryPhrase = queryNot = null;
        
        if(log.isDebugEnabled()){
            log.debug("AdvancedQueryBuilder: Query=" + query);
        }

    }

    /*
     * Append query to StringBuffer
     */
    private boolean myAppender(StringBuffer query,
                               String in,
                               String operator,
                               boolean prefix,
                               String prefixOperator) {

        String insert = insertOperator(in, operator, prefix, prefixOperator);
        if (insert.length()>0) {
            if( query.length()>0 ){
                query.append(' ');
            }
            query.append(insert);
            return true;
        }
        return false;
    }

    /*
     *  Surround queryPhrase with qoutes
     */
    private String insertPhrase(String queryPhrase, boolean prefix) {
        if (queryPhrase == null || queryPhrase.trim().equals("") ){
            return "";
        }
        String p = prefix ? " AND " : "";
        return p +  "\"" + queryPhrase + "\"";
    }

    /*
     * Replace duplicate whitespaces with one space only, then
     * substitue that space with " OPERATOR ".
     */
    private String insertOperator(String in, String operator, boolean prefixKeyword, String prefixOperator) {

        String original=in;
        if (log.isDebugEnabled()) {
            log.debug("ENTR: insertOperator()");
        }
        if (in == null || "".equals(in)) {
            return "";
        }
            // Remove duplicate whitespace
        in = trimDuplicateSpaces(in);
        in = in.replaceAll(" ", " " + operator + " ");

        if (prefixKeyword) {
            in = prefixOperator +" "+in;
        }
        in.trim();

        if (log.isDebugEnabled()) {
            log.debug("insertOperator: replaced "+ original + " => " + in);
        }
        return in;
    }

    /**
     * Remote duplicate spaces. Leading and trailing spaces will
     * be preserved
     * @param query that may conaint duplicate spaces
     * @return string with duplicate spaces removed
     */
    public static String trimDuplicateSpaces(String query){
        Log log = LogFactory.getLog(AdvancedQueryBuilder.class);

        if(log.isDebugEnabled()){
            log.debug("ENTR: trimDuplicateSpaces() query=" + query);
        }

        if(query == null){ return null; }
        if("".equals(query)) { return ""; }

        //query = query.trim();
        query = query.replaceAll("\\s+", " ");

        return query;
    }
}
