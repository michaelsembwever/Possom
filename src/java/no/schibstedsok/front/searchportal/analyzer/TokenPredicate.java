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


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class TokenPredicate implements Predicate, Comparable/*<TokenPredicate>*/ {
    
    private static final Map/*<String>,<TokenPredicate>*/ tokenMap = new Hashtable/*<String>,<TokenPredicate>*/();
    
    // Common predicates.
    // [TODO] TokenPredicate should be turned into a Java5 enum object with this list.
    public static final TokenPredicate ALWAYSTRUE = new TokenPredicate("alwaysTrue");
    public static final TokenPredicate EXACTFIRST = new TokenPredicate("exact_firstname");
    public static final TokenPredicate EXACTLAST = new TokenPredicate("exact_lastname");
    public static final TokenPredicate TNS = new TokenPredicate("tns");
    public static final TokenPredicate FIRSTNAME = new TokenPredicate("firstname");
    public static final TokenPredicate LASTNAME = new TokenPredicate("lastname");
    public static final TokenPredicate COMPANYNAME = new TokenPredicate("company");
    public static final TokenPredicate EXACTCOMPANYNAME = new TokenPredicate("exact_company");
    public static final TokenPredicate GEOLOCAL = new TokenPredicate("geolocal");
    public static final TokenPredicate GEOGLOBAL = new TokenPredicate("geoglobal");
    public static final TokenPredicate GEOLOCALEXACT = new TokenPredicate("exact_geolocal");
    public static final TokenPredicate GEOGLOBALEXACT = new TokenPredicate("exact_geoglobal");
    public static final TokenPredicate CATEGORY = new TokenPredicate("category");
    public static final TokenPredicate PRIOCOMPANYNAME = new TokenPredicate("companypriority");
    public static final TokenPredicate KEYWORD = new TokenPredicate("keyword");
    public static final TokenPredicate FULLNAME = new TokenPredicate("fullname"); // ?
    public static final TokenPredicate CATALOGUEPREFIX = new TokenPredicate("cataloguePrefix"); 
    public static final TokenPredicate WEATHERPREFIX = new TokenPredicate("weatherPrefix");
    public static final TokenPredicate PICTUREPREFIX = new TokenPredicate("picturePrefix");
    public static final TokenPredicate NEWSPREFIX = new TokenPredicate("newsPrefix");
    public static final TokenPredicate EXACTWIKI = new TokenPredicate("exact_wikino"); 
    public static final TokenPredicate WIKIPEDIAPREFIX = new TokenPredicate("wikipediaPrefix");
    public static final TokenPredicate ORGNR = new TokenPredicate("orgNr"); 
    public static final TokenPredicate WIKIPEDIA = new TokenPredicate("wikino"); 
    public static final TokenPredicate PHONENUMBER = new TokenPredicate("phoneNumber"); 
    public static final TokenPredicate ENGLISHWORDS = new TokenPredicate("international");
    public static final TokenPredicate TVPREFIX = new TokenPredicate("tvPrefix");
    public static final TokenPredicate COMPANYSUFFIX = new TokenPredicate("companySuffix");
    public static final TokenPredicate MATHPREDICATE = new TokenPredicate("mathExpression");    
    
    private final String token;


    private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY 
            = "Argument to evuluate must be an instance of a TokenEvaluatorFactory";

    /**
     * Create a new TokenPredicate that will return true iff token occurs in the
     * query.
     *
     * @param token     the token.
     */
    private TokenPredicate(final String token) {
        this.token = token;
        tokenMap.put(token,this);
    }
    
    public static TokenPredicate valueOf(final String token){
        return (TokenPredicate)tokenMap.get(token);
    }
    
    public static Collection/*<TokenPredicate>*/ getTokenPredicates(){
        return Collections.unmodifiableCollection(tokenMap.values());
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
