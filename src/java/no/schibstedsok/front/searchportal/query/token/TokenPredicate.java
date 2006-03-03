// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.Predicate;

/** Implementation of org.apache.commons.collections.Predicate for the terms in the Query.
 * Predicates use TokenEvaluators to prove the Predicate's validity to the Query.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TokenPredicate implements Predicate, Comparable/*<TokenPredicate>*/ {

    public static final class FastTokenPredicate extends TokenPredicate {
        public  FastTokenPredicate(final String token) {
            super(token);
            FAST_TOKENS.add( this );
        }
    }
    public static final class RegExpTokenPredicate extends TokenPredicate {
        public  RegExpTokenPredicate(final String token) {
            super(token);
        }
    }

    private static final Map/*<String>,<TokenPredicate>*/ TOKEN_MAP = new Hashtable/*<String>,<TokenPredicate>*/();
    private static final Set/*<TokenPredicate>*/ FAST_TOKENS = new HashSet/*<TokenPredicate>*/();

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
    public static final FastTokenPredicate TOP3EXACT = new FastTokenPredicate("exact_top3boosts");
    
    // RegExp TokenPredicates
    public static final RegExpTokenPredicate CATALOGUEPREFIX = new RegExpTokenPredicate("cataloguePrefix");
    public static final RegExpTokenPredicate COMPANYSUFFIX = new RegExpTokenPredicate("companySuffix");
    public static final RegExpTokenPredicate MATHPREDICATE = new RegExpTokenPredicate("mathExpression");
    public static final RegExpTokenPredicate NEWSPREFIX = new RegExpTokenPredicate("newsPrefix");
    public static final RegExpTokenPredicate ORGNR = new RegExpTokenPredicate("orgNr");
    public static final RegExpTokenPredicate PICTUREPREFIX = new RegExpTokenPredicate("picturePrefix");
    public static final RegExpTokenPredicate PHONENUMBER = new RegExpTokenPredicate("phoneNumber");
    public static final RegExpTokenPredicate SITEPREFIX = new RegExpTokenPredicate("sitePrefix");
    public static final RegExpTokenPredicate TVPREFIX = new RegExpTokenPredicate("tvPrefix");
    public static final RegExpTokenPredicate WEATHERPREFIX = new RegExpTokenPredicate("weatherPrefix");
    public static final RegExpTokenPredicate WIKIPEDIAPREFIX = new RegExpTokenPredicate("wikipediaPrefix");
    public static final RegExpTokenPredicate ONLYSKIINFOPREFIX = new RegExpTokenPredicate("onlySkiInfoPrefix");
    public static final RegExpTokenPredicate SKIINFOPREFIX = new RegExpTokenPredicate("skiInfoPrefix");
    public static final RegExpTokenPredicate EMPTYQUERY = new RegExpTokenPredicate("emptyQuery");

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
        TOKEN_MAP.put(token, this);
    }

    /** Public method to find the correct TokenPredicate given the Token's string.
     */
    public static TokenPredicate valueOf(final String token) {
        return (TokenPredicate) TOKEN_MAP.get(token);
    }

    /** Utility method to use all TokenPredicates in existance.
     */
    public static Collection/*<TokenPredicate>*/ getTokenPredicates() {
        return Collections.unmodifiableCollection(TOKEN_MAP.values());
    }

    /** Utility method to use all FastTokenPredicates in existance.
     */
    public static Set/*<TokenPredicate>*/ getFastTokenPredicates() {
        return Collections.unmodifiableSet(FAST_TOKENS);
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
        if ( ! (evalFactory instanceof TokenEvaluatorFactory) ) {
            throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
        }
        // process
        final TokenEvaluatorFactory factory = (TokenEvaluatorFactory) evalFactory;
        
        // check that the evaluation hasn't already been done
        // we can only check against the knownPredicates because with the possiblePredicates we are not sure whether 
        //  the evaluation is for the building of the known and possible predicate list (during query parsing)(in which
        //  case we could perform the check) or if we are scoring and need to know if the possible predicate is really
        //  applicable now (in the context of the whole query).
        final Set/*<Predicate>*/ knownPredicates = factory.getClausesKnownPredicates();
        if( null != knownPredicates && knownPredicates.contains(this) ){
            return true;
        }
        
        final String query = factory.getQueryString();
        final TokenEvaluator evaluator = factory.getEvaluator(this);
        return evaluator.evaluateToken(token, factory.getCurrentTerm(), query);
    }

    /** {@inheritDoc}
     */
    public int compareTo(final Object/*TokenPredicate*/ obj) {
        // pre-condition check
        if ( !(obj instanceof TokenPredicate) ) {
            throw new IllegalArgumentException("Will not compare against object of type " + obj.getClass().getName());
        }
        // compare
        final TokenPredicate tp = (TokenPredicate) obj;
        return token.compareTo(tp.token);
    }

    /** {@inheritDoc}
     */
    public String toString() {
        return "TokenPredicate: " + token;
    }


}
