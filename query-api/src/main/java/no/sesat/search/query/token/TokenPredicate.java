/* Copyright (2005-2007) Schibsted Søk AS
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
    // TODO make ExactFast tokens a separate Type referencing the original Fast token.
    ANIMAL (Type.FAST),
    BLOCKET (Type.FAST), // FIXME!! What is blocket? we do not put specific names into this list
    CATEGORY (Type.FAST),
    CELEBRITY (Type.FAST),
    CLASSIFIED_CATEGORY (Type.FAST),
    COMPANY_CHAIN (Type.FAST),
    COMPANYENRICHMENT (Type.FAST),
    EXACT_COMPANYENRICHMENT (Type.FAST),
    COMPANY_KEYWORD (Type.FAST),
    COMPANY_KEYWORD_RESERVED (Type.FAST),
//    COMPANYRANK (Type.FAST), // not used!
//    EXACT_COMPANYRANK (Type.FAST), // not used!
    DISEASE (Type.FAST),
    ENGLISHWORDS (Type.FAST),
    GEOLOCAL (Type.FAST),
    GEOGLOBAL (Type.FAST),
    EXACT_GEOLOCAL (Type.FAST),
    EXACT_GEOGLOBAL (Type.FAST),
    FIRSTNAME (Type.FAST),
    FOOD (Type.FAST),
    EXACT_FIRST (Type.FAST),
    FULLNAME (Type.FAST),
    EXACT_FULLNAME (Type.FAST),
    LASTNAME (Type.FAST),
    EXACT_LAST (Type.FAST),
    MATERIAL (Type.FAST),
    PRIOCOMPANYNAME (Type.FAST),
    EXACT_PRIOCOMPANYNAME (Type.FAST),
    PRODUCT_BICYCLE (Type.FAST),
    PRODUCT_CAR (Type.FAST),
    PRODUCT_CHILDREN (Type.FAST),
    PRODUCT_CLOTHING (Type.FAST),
    PRODUCT_CONSTRUCTION (Type.FAST),
    PRODUCT_COSTUME (Type.FAST),
    PRODUCT_ELECTRONIC (Type.FAST),
    PRODUCT_FURNITURE (Type.FAST),
    PRODUCT_GARDEN (Type.FAST),
    PRODUCT_HOBBY (Type.FAST),
    PRODUCT_HOUSEHOLD (Type.FAST),
    PRODUCT_JEWELRY (Type.FAST),
    PRODUCT_MOTOR (Type.FAST),
    PRODUCT_MUSIC (Type.FAST),
    PRODUCT_SHOE (Type.FAST),
    PRODUCT_SPORT (Type.FAST),
    PRODUCT_WATCH (Type.FAST),
    PRODUCT_WEAPON (Type.FAST),
    OCEAN (Type.FAST),
    EXACT_TOP3 (Type.FAST),
    EXACT_PPCTOPLIST (Type.FAST),
    STOCKMARKETTICKERS (Type.FAST),
    STOCKMARKETFIRMS (Type.FAST),
    EXACT_STOCKMARKETTICKERS (Type.FAST),
    EXACT_STOCKMARKETFIRMS (Type.FAST),
    STYLE (Type.FAST),
    TNS (Type.FAST),
    TRADEMARK (Type.FAST),
    WIKIPEDIA (Type.FAST),
    EXACT_WIKI (Type.FAST),

    IMAGES (Type.FAST),
    EXACT_IMAGE (Type.FAST),

    PRISJAKT_CATEGORIES_AND_MANUFACTURERS (Type.FAST),
    PRISJAKT_CATEGORIES (Type.FAST),
    PRISJAKT_MANUFACTURERS (Type.FAST),
    PRISJAKT_PRODUCTS (Type.FAST),
    PRISJAKT_SHOPS (Type.FAST),


    // RegExp TokenPredicates -- magic words
    BOOK_MAGIC (Type.REGEX),
    CATALOGUE_MAGIC (Type.REGEX),
    CLASSIFIED_MAGIC (Type.REGEX),
    EXACT_CLASSIFIED_MAGIC (Type.REGEX),
    CULTURE_MAGIC (Type.REGEX),
    MOVIE_MAGIC (Type.REGEX),
    NEWS_MAGIC (Type.REGEX),
    OCEAN_MAGIC (Type.REGEX),
    PICTURE_MAGIC (Type.REGEX),
    VIDEO_MAGIC (Type.REGEX),
    RECEIPE_MAGIC (Type.REGEX),
    SKIINFO_MAGIC (Type.REGEX),
    STOCK_MAGIC (Type.REGEX),
    TV_MAGIC (Type.REGEX),
    WEATHER_MAGIC (Type.REGEX),
    WEBTV_MAGIC (Type.REGEX),
    WHITE_MAGIC (Type.REGEX),
    WIKIPEDIA_MAGIC (Type.REGEX),
    YELLOW_MAGIC (Type.REGEX),
    MAP_MAGIC(Type.REGEX),
    

    // RegExp TokenPredicates -- trigger words/phrases
    CATALOGUE_TRIGGER (Type.REGEX),
    CLASSIFIED_TRIGGER (Type.FAST),
    LOAN_TRIGGER (Type.REGEX),
    NEWS_TRIGGER (Type.REGEX),
    OCEAN_TRIGGER(Type.REGEX),
    PICTURE_TRIGGER (Type.REGEX),
    VIDEO_TRIGGER (Type.REGEX),
    SKIINFO_TRIGGER (Type.REGEX),
    SUDOKU_TRIGGER (Type.REGEX),
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

    /** JepTokenPredicate. **/
    MATHPREDICATE (Type.JEP);

    /** The types of TokenPredicates that exist.
     */
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
    }

    // instance fields

    private final Type type;


    private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY
            = "Argument to evaluate must be an instance of a TokenEvaluationEngine";
    private static final String ERR_TOKEN_NOT_FOUND = "Token argument not found ";
    private static final String ERR_METHOD_CLOSED_TO_OTHER_THREADS
            = "TokenPredicate.evaluate(..) can only be used by same thread that created TokenEvaluationEngine!";
    private static final String ERR_ENGINE_MISSING_STATE = "TokenEvaluationEngine must have state assigned";

    /**
     * Create a new TokenPredicate that will return true if it applies to the
     * query.
     *
     * @param type     the fastListName type.
     */
    TokenPredicate(final Type type) {

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
                Static.FAST_TOKENS.add(this);
                break;
            default:
                break;
        }
    }

    /**
     *
     * @return
     */
    public Type getType(){
        return type;
    }

    /** Utility method to use all TokenPredicates in existence.
     * @return
     */
    public static Collection<TokenPredicate> getTokenPredicates() {
        return Collections.unmodifiableCollection(Arrays.asList(values()));
    }

    /** Utility method to use all FastTokenPredicates in existence.
     * @return
     */
    public static Set<TokenPredicate> getFastTokenPredicates() {
        return Collections.unmodifiableSet(Static.FAST_TOKENS);
    }

    /** Utility method to use all MagicTokenPredicates in existence.
     * @return
     */
    public static Set<TokenPredicate> getMagicTokenPredicates() {
        return Collections.unmodifiableSet(Static.MAGIC_TOKENS);
    }

    /** Utility method to use all TriggerTokenPredicates in existence.
     * @return
     */
    public static Set<TokenPredicate> getTriggerTokenPredicates() {
        return Collections.unmodifiableSet(Static.TRIGGER_TOKENS);
    }

    /**
     * Evaluates to true if fastListName occurs in the query. This method uses a
     * TokenEvaluationEngine to get a TokenEvaluator.
     *
     * <b>This method can only be called from the RunningQuery thread, not spawned search commands.</b>
     *
     * @param evalFactory
     *            The TTokenEvaluationEngineused to get a TokenEvaluator for
     *            this fastListName, AND to get the current term in the query being tokenised.
     * @return true if, according to the TokenEvaluator provided by the
     *         TokTokenEvaluationEngineastListName evaluates to true.
     */
    public boolean evaluate(final Object evalFactory) {
        // pre-condition checks
        if (! (evalFactory instanceof TokenEvaluationEngine)) {
            throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
        }
        // process
        final TokenEvaluationEngine engine = (TokenEvaluationEngine) evalFactory;
        if(Thread.currentThread() != engine.getOwningThread()){
            throw new IllegalStateException(ERR_METHOD_CLOSED_TO_OTHER_THREADS);
        }

        try{

            // check that the evaluation hasn't already been done
            // we can only check against the knownPredicates because with the possiblePredicates we are not sure whether
            //  the evaluation is for the building of the known and possible predicate list
            //    (during query parsing)(in which
            //  case we could perform the check) or if we are scoring and need to know if the
            //    possible predicate is really
            //  applicable now (in the context of the whole query).
            final Set<TokenPredicate> knownPredicates = engine.getState().getKnownPredicates();
            if(null != knownPredicates && knownPredicates.contains(this)){
                return true;
            }

            final TokenEvaluator evaluator = engine.getEvaluator(this);

            if(null != engine.getState().getTerm()){

                // Single term or clause evaluation
                return evaluator.evaluateToken(this, engine.getState().getTerm(), engine.getQueryString());

            }else if(null != engine.getState().getQuery()){

                // Whole query evaluation
                return engine.getState().getPossiblePredicates().contains(this)
                        && evaluator.evaluateToken(this, null, engine.getQueryString());

            }

        }catch(VeryFastListQueryException ie){
            // unfortunately Predicate.evaluate(..) does not declare to throw any checked exceptions.
            //  so we must sneak the VeryFastListQueryException through as a run-time exception.
            throw new EvaluationException(ie);
        }

        throw new IllegalStateException(ERR_ENGINE_MISSING_STATE);
    }

    public static final class EvaluationException extends RuntimeException{
        public EvaluationException(final VeryFastListQueryException vflqe){
            super(vflqe);
        }
    }

}
