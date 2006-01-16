// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.analyzer;

import java.lang.IllegalArgumentException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;


/** Implementation of org.apache.commons.collections.Predicate for the terms in the Query.
 * Predicates use TokenEvaluators to prove the Predicate's validity to the Query.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TokenPredicate implements Predicate, Comparable/*<TokenPredicate>*/ {
    
    public static final class FastTokenPredicate extends TokenPredicate{
        public  FastTokenPredicate(final String token){
            super(token);
        }
    }
    public static final class RegExpTokenPredicate extends TokenPredicate{
        public  RegExpTokenPredicate(final String token){
            super(token);
        }
    }    
    
    private static final Map/*<String>,<TokenPredicate>*/ tokenMap = new Hashtable/*<String>,<TokenPredicate>*/();
    private static final Set/*<TokenPredicate>*/ fastTokens = new HashSet/*<TokenPredicate>*/();
    
    // Common predicates.
    // [TODO] TokenPredicate should be turned into a Java5 enum object with this list.
    
    public static final TokenPredicate ALWAYSTRUE = new TokenPredicate("alwaysTrue");
    
    // Fast TokenPredicates
    public static final FastTokenPredicate EXACTFIRST = new FastTokenPredicate("exact_firstname");
    public static final FastTokenPredicate EXACTLAST = new FastTokenPredicate("exact_lastname");
    public static final FastTokenPredicate TNS = new FastTokenPredicate("tns");
    public static final FastTokenPredicate FIRSTNAME = new FastTokenPredicate("firstname");
    public static final FastTokenPredicate LASTNAME = new FastTokenPredicate("lastname");
    public static final FastTokenPredicate COMPANYNAME = new FastTokenPredicate("company");
    public static final FastTokenPredicate EXACTCOMPANYNAME = new FastTokenPredicate("exact_company");
    public static final FastTokenPredicate GEOLOCAL = new FastTokenPredicate("geolocal");
    public static final FastTokenPredicate GEOGLOBAL = new FastTokenPredicate("geoglobal");
    public static final FastTokenPredicate GEOLOCALEXACT = new FastTokenPredicate("exact_geolocal");
    public static final FastTokenPredicate GEOGLOBALEXACT = new FastTokenPredicate("exact_geoglobal");
    public static final FastTokenPredicate CATEGORY = new FastTokenPredicate("category");
    public static final FastTokenPredicate PRIOCOMPANYNAME = new FastTokenPredicate("companypriority");
    public static final FastTokenPredicate KEYWORD = new FastTokenPredicate("keyword");
    public static final FastTokenPredicate FULLNAME = new FastTokenPredicate("fullname"); // ?
    public static final FastTokenPredicate EXACTWIKI = new FastTokenPredicate("exact_wikino"); 
    public static final FastTokenPredicate WIKIPEDIA = new FastTokenPredicate("wikino"); 
    public static final FastTokenPredicate ENGLISHWORDS = new FastTokenPredicate("international");
    
    // RegExp TokenPredicates
    public static final RegExpTokenPredicate CATALOGUEPREFIX = new RegExpTokenPredicate("cataloguePrefix"); 
    public static final RegExpTokenPredicate COMPANYSUFFIX = new RegExpTokenPredicate("companySuffix");
    public static final RegExpTokenPredicate MATHPREDICATE = new RegExpTokenPredicate("mathExpression");   
    public static final RegExpTokenPredicate NEWSPREFIX = new RegExpTokenPredicate("newsPrefix");
    public static final RegExpTokenPredicate ORGNR = new RegExpTokenPredicate("orgNr"); 
    public static final RegExpTokenPredicate PICTUREPREFIX = new RegExpTokenPredicate("picturePrefix");
    public static final RegExpTokenPredicate PHONENUMBER = new RegExpTokenPredicate("phoneNumber"); 
    public static final RegExpTokenPredicate TVPREFIX = new RegExpTokenPredicate("tvPrefix");
    public static final RegExpTokenPredicate WEATHERPREFIX = new RegExpTokenPredicate("weatherPrefix");
    public static final RegExpTokenPredicate WIKIPEDIAPREFIX = new RegExpTokenPredicate("wikipediaPrefix");
    
    // instance fields
    private final String token;


    private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY 
            = "Argument to evuluate must be an instance of a TokenEvaluatorFactory";

    /**
     * Create a new TokenPredicate that will return true iff token occurs in the
     * query.
     *
     * @param token     the token.
     */
    protected TokenPredicate(final String token) {
        this.token = token;
        tokenMap.put(token,this);
        if( this instanceof FastTokenPredicate ){
            fastTokens.add(this);
        }
    }
    
    /** Public method to find the correct TokenPredicate given the Token's string.
     */
    public static TokenPredicate valueOf(final String token){
        return (TokenPredicate)tokenMap.get(token);
    }
    
    /** Utility method to use all TokenPredicates in existance. 
     */
    public static Collection/*<TokenPredicate>*/ getTokenPredicates(){
        return Collections.unmodifiableCollection(tokenMap.values());
    }
    
    /** Utility method to use all FastTokenPredicates in existance. 
     */
    public static Set/*<TokenPredicate>*/ getFastTokenPredicates(){
        return Collections.unmodifiableSet(fastTokens);
    }

    /**
     * Evaluates to true if token occurs in the query. This method uses a
     * TokenEvaluatorFactory to get a TokenEvaluator
     *
     * @param evalFactory
     *            The TokenEvaluatorFactory used to get a TokenEvaluator for
     *            this token, AND to get the current term in the query being tokenised.
     *
     * @return true if, according to the TokenEvaluator provided by the
     *         TokenEvaluatorFactory, token evaluates to true.
     */
    public boolean evaluate(final Object evalFactory) {
        // pre-condition check
        if( ! (evalFactory instanceof TokenEvaluatorFactory) ){
            throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
        }
        // process
        final TokenEvaluatorFactory factory = (TokenEvaluatorFactory) evalFactory;
        final String query = factory.getQueryString();
        return factory.getEvaluator(this).evaluateToken(token, factory.getCurrentTerm(), query); 
    }

    public int compareTo(final Object/*TokenPredicate*/ obj) {
        // pre-condition check
        if( !(obj instanceof TokenPredicate) ){
            throw new IllegalArgumentException("Will not compare against object of type "+obj.getClass().getName());
        }
        // compare
        final TokenPredicate tp = (TokenPredicate)obj;
        return token.compareTo(tp.token);
    }

    public String toString() {
        return "TokenPredicate: "+token;
    }
    

}
