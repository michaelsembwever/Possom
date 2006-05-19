// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.Predicate;

/** Implementation of org.apache.commons.collections.Predicate for the terms in the Query.
 * Predicates use TokenEvaluators to prove the Predicate's validity to the Query.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public enum TokenPredicate implements Predicate {

    // Common predicates.
    ALWAYSTRUE (Type.GENERIC),

    // Fast TokenPredicates
    //  full list can be found at sch-login01.osl.basefarm.net:/www/schibstedsok/home/ssmojaco/analyselister
    ANIMAL (Type.FAST, "animal"),
    CATEGORY (Type.FAST, "category"),
    CELEBRITY (Type.FAST, "celebrity"),
    CHAIN (Type.FAST, "chain"),
    COMPANYENRICHMENT (Type.FAST, "companyenrich"),
    EXACTCOMPANYENRICHMENT (Type.FAST, "exact_companyenrich"),
    COMPANYRANK (Type.FAST, "companyrank"),
    EXACTCOMPANYRANK (Type.FAST, "exact_companyrank"),
    DISEASE (Type.FAST, "disease"),
    ENGLISHWORDS (Type.FAST, "international"),
    KEYWORD (Type.FAST, "keyword"),
    GEOLOCAL (Type.FAST, "geolocal"),
    GEOGLOBAL (Type.FAST, "geoglobal"),
    GEOLOCALEXACT (Type.FAST, "exact_geolocal"),
    GEOGLOBALEXACT (Type.FAST, "exact_geoglobal"),
    FIRSTNAME (Type.FAST, "firstname"),
    FOOD (Type.FAST, "food"),
    EXACTFIRST (Type.FAST, "exact_firstname"),
    FULLNAME (Type.FAST, "fullname"),
    LASTNAME (Type.FAST, "lastname"),
    EXACTLAST (Type.FAST, "exact_lastname"),
    PRIOCOMPANYNAME (Type.FAST, "companypriority"),
    TOP3EXACT (Type.FAST, "exact_top3boosts"),
    EXACT_PPCTOPLIST (Type.FAST, "exact_ppctoplist"),
    STOCKMARKETTICKERS (Type.FAST, "stockmarkettickers"),
    STOCKMARKETFIRMS (Type.FAST, "stockmarketfirms"),
    EXACT_STOCKMARKETTICKERS (Type.FAST, "exact_stockmarkettickers"),
    EXACT_STOCKMARKETFIRMS (Type.FAST, "exact_stockmarketfirms"),
    WIKIPEDIA (Type.FAST, "wikino"),
    EXACTWIKI (Type.FAST, "exact_wikino"),
    TNS (Type.FAST, "tns"),

    // RegExp TokenPredicates -- magic words
    BOOK_MAGIC (Type.REGEX),
    CATALOGUE_MAGIC (Type.REGEX),
    CULTURE_MAGIC (Type.REGEX),
    MOVIE_MAGIC (Type.REGEX),
    NEWS_MAGIC (Type.REGEX),
    OCEAN_MAGIC (Type.REGEX),
    PICTURE_MAGIC (Type.REGEX),
    RECEIPE_MAGIC (Type.REGEX),
    SKIINFO_MAGIC (Type.REGEX),
    STOCK_MAGIC (Type.REGEX),
    TV_MAGIC (Type.REGEX),
    WEATHER_MAGIC (Type.REGEX),
    WEBTV_MAGIC (Type.REGEX),
    WIKIPEDIA_MAGIC (Type.REGEX),

    // RegExp TokenPredicates -- trigger words/phrases
    CATALOGUE_TRIGGER (Type.REGEX),
    NEWS_TRIGGER (Type.REGEX),
    PICTURE_TRIGGER (Type.REGEX),
    SKIINFO_TRIGGER (Type.REGEX),
    TV_TRIGGER (Type.REGEX),
    WEATHER_TRIGGER (Type.REGEX),
    WIKIPEDIA_TRIGGER (Type.REGEX),

    // RegExp TokenPredicates -- prefixes
    SITEPREFIX (Type.REGEX),

    // RegExp TokenPredicates -- suffixes
    COMPANYSUFFIX (Type.REGEX),

    // RegExp TokenPredicates -- general expression
    ORGNR (Type.REGEX),
    PHONENUMBER (Type.REGEX),
    ONLYSKIINFO (Type.REGEX),
    EMPTYQUERY (Type.REGEX),
    LOAN_TRIGGER (Type.REGEX),
    SUDOKU_TRIGGER (Type.REGEX),
    
    // JepTokenPredicate
    MATHPREDICATE (Type.JEP);

    // The types of TokenPredicates that exist
    public enum Type { GENERIC, FAST, REGEX, JEP }

    /**
     * Because the enum declarations must come first and they are static,
     *      their constructor when referencing other static members are referencing them
     *      before they themselves have been statically initialised.
     * That is without the wrapping class declaration to FAST_TOKENS it would have a value
     *      of null when referenced to in the constructor.
     * By wrapping it inside an inner class because all static initialisors of the inner class are run first
     *      it ensures FAST_TOKENS will not be null.
     **/
    private static final class Static{
        public static final Set<TokenPredicate> MAGIC_TOKENS = new HashSet<TokenPredicate>();
        public static final Set<TokenPredicate> TRIGGER_TOKENS = new HashSet<TokenPredicate>();
        public static final Set<TokenPredicate> FAST_TOKENS = new HashSet<TokenPredicate>();

        public static final String ERR_FAST_TYPES_MUST_SPECIFY_LISTNAME = "Illegal constructor used for Type.FAST";
        public static final String ERR_FAST_TYPES_ONLY_CONSTRUCTOR = "Constructor used only appropriate for Type.FAST";
    }

    // instance fields
    private final String fastListName;
    private final Type type;


    private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY
            = "Argument to evuluate must be an instance of a TokenEvaluatorFactory";
    private static final String ERR_TOKEN_NOT_FOUND = "Token argument not found ";


    /**
     * Create a new TokenPredicate that will return true if fastListName occurs in the
     * query.
     *
     * @param type     the fastListName type.
     */
    TokenPredicate(final Type type) {
        fastListName = null;
        this.type = type;

        switch(type){
            case REGEX:
                if(name().endsWith("_MAGIC")){
                    Static.MAGIC_TOKENS.add(this);

                }else if(name().endsWith("_TRIGGER")){
                    Static.TRIGGER_TOKENS.add(this);
                }
                break;
            case FAST:
                throw new IllegalArgumentException(Static.ERR_FAST_TYPES_MUST_SPECIFY_LISTNAME);
            default:
                break;
        }
    }

    /**
     * Create a new TokenPredicate that will return true if fastListName occurs in the
     * query.
     * Only use for fastListName of type FAST.
     *
     * @param type     the fastListName type.
     * @param fastListName     the fastListName.
     */
    TokenPredicate(final Type type, final String fastListName) {
        this.fastListName = fastListName;
        this.type = type;
        switch(type){
            case FAST:
                Static.FAST_TOKENS.add(this);
                break;
            default:
                throw new IllegalArgumentException(Static.ERR_FAST_TYPES_ONLY_CONSTRUCTOR);
        }
    }

    public Type getType(){
        return type;
    }

    String getFastListName(){
        return fastListName;
    }

    /** Public method to find the correct FAST TokenPredicate given the Token's string.
     */
    public static TokenPredicate valueFor(final String fastListName) {

        for(TokenPredicate tp : Static.FAST_TOKENS){
            if(fastListName.equals(tp.fastListName)){
                return tp;
            }
        }
        throw new IllegalArgumentException(ERR_TOKEN_NOT_FOUND + fastListName);
    }

    /** Utility method to use all TokenPredicates in existance.
     */
    public static Collection<TokenPredicate> getTokenPredicates() {
        return Collections.unmodifiableCollection(Arrays.asList(values()));
    }

    /** Utility method to use all FastTokenPredicates in existance.
     */
    public static Set<TokenPredicate> getFastTokenPredicates() {
        return Collections.unmodifiableSet(Static.FAST_TOKENS);
    }

    /** Utility method to use all MagicTokenPredicates in existance.
     */
    public static Set<TokenPredicate> getMagicTokenPredicates() {
        return Collections.unmodifiableSet(Static.MAGIC_TOKENS);
    }

    /** Utility method to use all TriggerTokenPredicates in existance.
     */
    public static Set<TokenPredicate> getTriggerTokenPredicates() {
        return Collections.unmodifiableSet(Static.TRIGGER_TOKENS);
    }

    /**
     * Evaluates to true if fastListName occurs in the query. This method uses a
     * TokenEvaluatorFactory to get a TokenEvaluator
     *
     * @param evalFactory
     *            The TokenEvaluatorFactory used to get a TokenEvaluator for
     *            this fastListName, AND to get the current term in the query being tokenised.
     *
     * @return true if, according to the TokenEvaluator provided by the
     *         TokenEvaluatorFactory, fastListName evaluates to true.
     */
    public boolean evaluate(final Object evalFactory) {
        // pre-condition check
        if (! (evalFactory instanceof TokenEvaluatorFactory)) {
            throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
        }
        // process
        final TokenEvaluatorFactory factory = (TokenEvaluatorFactory) evalFactory;

        // check that the evaluation hasn't already been done
        // we can only check against the knownPredicates because with the possiblePredicates we are not sure whether
        //  the evaluation is for the building of the known and possible predicate list (during query parsing)(in which
        //  case we could perform the check) or if we are scoring and need to know if the possible predicate is really
        //  applicable now (in the context of the whole query).
        final Set<TokenPredicate> knownPredicates = factory.getClausesKnownPredicates();
        if(null != knownPredicates && knownPredicates.contains(this)){
            return true;
        }

        final String query = factory.getQueryString();
        final TokenEvaluator evaluator = factory.getEvaluator(this);
        return evaluator.evaluateToken(fastListName, factory.getCurrentTerm(), query);
    }

}
