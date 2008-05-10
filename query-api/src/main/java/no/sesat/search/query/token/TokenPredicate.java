/* Copyright (2005-2008) Schibsted Søk AS
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


import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.collections.Predicate;

/** A Categorisation of knowledge that attaches itself
 *   as meta-data to words and groups of words (clauses) within a query. <br/><br/>
 *
 *  <br/><br/>
 * Implementation of org.apache.commons.collections.Predicate for the terms in the Query.
 * Predicates use TokenEvaluators to prove the Predicate's validity to the Query.
 *
 * @todo improve design. inner classes providing functionality inside an interface is frowned upon.
 *
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface TokenPredicate extends Predicate, Serializable{

    /** The types of TokenPredicates that exist.
     * @todo will need to become a class that can be extended. SEARCH-3540. a mapping to the Evaluation implementation.
     */
    static final class Type implements Serializable{

        public static final Type BOOLEAN = new Type("BOOLEAN", null);
        public static final Type FAST = new Type("FAST", VeryFastTokenEvaluator.class);
        public static final Type REGEX = new Type("REGEX", RegExpTokenEvaluator.class);
        public static final Type JEP = new Type("JEP", JepTokenEvaluator.class);

        private final String name;
        private final Class<? extends TokenEvaluator> cls;

        public Type(final String name, final Class<? extends TokenEvaluator> cls){

            this.name = name;
            this.cls = cls;
            TokenPredicateImpl.TOKENS_BY_TYPE.put(this, new CopyOnWriteArraySet<TokenPredicate>());
        }
    }

    /** The name of the TokenPredicate. Must be uppercase. Must be unique across all skins.
     *
     * @return TokenPredicate name.
     */
    String name();

    /** The type of the TokenPredicate. TokenEvaluationEngine will use this to determine which TokenEvaluator to use.
     *
     * @return the type
     */
    Type getType();

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
    boolean evaluate(Object evalFactory);

    /** A token predicate that requires an exact match against the whole query.
     * A exact peer instance must return itself.
     *
     * @return
     */
    TokenPredicate exactPeer();

    // Inner Classes -----------------------------------------------------

    /** A formalised breakdown of metadata categories that search terms can match.
     *
     * The break down of these categories should roughly follow what is found at
     *   http://en.wikipedia.org/wiki/Portal:Contents/Categorical_index
     */
    enum Categories implements TokenPredicate {

        // Categorical TokenPredicates
        // TODO determine type automatically. skins maybe choose an alternative type. SEARCH-3540.

        /** */
        ANIMAL (Type.FAST),
        /** @deprecated FIXME!! What is blocket? we do not put specific names into this list */
        BLOCKET (Type.FAST),
        /** @todo rename to COMPANY_CATEGORY **/
        CATEGORY (Type.FAST),
        /** */
        CELEBRITY (Type.FAST),
        /** */
        COMPANYBRANCH (Type.FAST),
        /** */
        COMPANYBRANCHKEYWORD (Type.FAST),
        /** */
        CLASSIFIED_CATEGORY (Type.FAST),
        /** */
        COMPANY_CHAIN (Type.FAST),
        /** @todo rename to just COMPANY */
        COMPANYENRICHMENT (Type.FAST),
        /** */
        COMPANY_KEYWORD (Type.FAST),
        /** */
        COMPANY_KEYWORD_RESERVED (Type.FAST),
        /** */
        BIGCOMPANY (Type.FAST),
        /** */
        DISEASE (Type.FAST),
        /** */
        ENGLISHWORDS (Type.FAST),
        /** */
        GEOLOCAL (Type.FAST),
        /** */
        GEOGLOBAL (Type.FAST),
        /** */
        GEO_BOROUGH (Type.FAST),
        /** */
        GEO_COUNTY (Type.FAST),
        /** */
        GEO_STREET (Type.FAST),
        /** */
        GEO_MUNICIPALITY (Type.FAST),
        /** */
        GEO_AREA (Type.FAST),
        /** */
        GEO_ZIPCODE (Type.FAST),
        /** */
        GEO_POSTALPLACE (Type.FAST), /** TODO: RENAME! */
        /** */
        FIRSTNAME (Type.FAST),
        /** */
        FOOD (Type.FAST),
        /** */
        FULLNAME (Type.FAST),
        /** */
        LASTNAME (Type.FAST),
        /** */
        MATERIAL (Type.FAST),
        /** @deprecated remove. use MOVIE_TITLE TokenPredicates instead. */
        MOVIE (Type.FAST),
        /** */
        MOVIE_TITLE(Type.FAST),
        /** */
        MOVIE_ACTOR(Type.FAST),
        /** */
        MOVIE_DIRECTOR(Type.FAST),
        /** */
        NEWSCASE (Type.FAST),
        /** */
        NOPICTURE (Type.FAST),
        /** */
        PICTURE (Type.FAST),
        /** */
        PRIOCOMPANYNAME (Type.FAST),
        /** */
        PRODUCT_BICYCLE (Type.FAST),
        /** */
        PRODUCT_CAR (Type.FAST),
        /** */
        PRODUCT_CHILDREN (Type.FAST),
        /** */
        PRODUCT_CLOTHING (Type.FAST),
        /** */
        PRODUCT_CONSTRUCTION (Type.FAST),
        /** */
        PRODUCT_COSTUME (Type.FAST),
        /** */
        PRODUCT_ELECTRONIC (Type.FAST),
        /** */
        PRODUCT_FURNITURE (Type.FAST),
        /** */
        PRODUCT_GARDEN (Type.FAST),
        /** */
        PRODUCT_HOBBY (Type.FAST),
        /** */
        PRODUCT_HOUSEHOLD (Type.FAST),
        /** */
        PRODUCT_JEWELRY (Type.FAST),
        /** */
        PRODUCT_MOTOR (Type.FAST),
        /** */
        PRODUCT_MUSIC (Type.FAST),
        /** */
        PRODUCT_SHOE (Type.FAST),
        /** */
        PRODUCT_SPORT (Type.FAST),
        /** */
        PRODUCT_WATCH (Type.FAST),
        /** */
        PRODUCT_WEAPON (Type.FAST),
        /** */
        PRODUCT_TORGET (Type.FAST),
        /** */
        PROFESSION(Type.FAST),
        /** */
        OCEAN (Type.FAST),
        /** */
        STOCKMARKETTICKERS (Type.FAST),
        /** */
        STOCKMARKETFIRMS (Type.FAST),
        /** */
        STYLE (Type.FAST),
        /** */
        TNS (Type.FAST),
        /** */
        TVPROGRAM (Type.FAST),
        /** */
        TVCHANNEL (Type.FAST),
        /** */
        TRADEMARK (Type.FAST),
        /** */
        WIKIPEDIA (Type.FAST),
        /** */

        ARTIST (Type.FAST),
        FICTION_CHARACTER (Type.FAST),
        MOTOR_SPORT (Type.FAST),
        PUBLIC_SERVICE_BROADCASTING (Type.FAST),

        IMAGES (Type.FAST),
        /** @deprecated FIXME!! What is prisjakt? we do not put specific names into this list */
        PRISJAKT_CATEGORIES_AND_MANUFACTURERS (Type.FAST),
        /** @deprecated FIXME!! What is prisjakt? we do not put specific names into this list */
        PRISJAKT_CATEGORIES (Type.FAST),
        /** @deprecated FIXME!! What is prisjakt? we do not put specific names into this list */
        PRISJAKT_MANUFACTURERS (Type.FAST),
        /** @deprecated FIXME!! What is prisjakt? we do not put specific names into this list */
        PRISJAKT_PRODUCTS (Type.FAST),
        /** @deprecated FIXME!! What is prisjakt? we do not put specific names into this list */
        PRISJAKT_SHOPS (Type.FAST),


        // RegExp TokenPredicates -- magic words
        BOOK_MAGIC (Type.REGEX),
        /** */
        CATALOGUE_MAGIC (Type.REGEX),
        /** */
        CLASSIFIED_MAGIC (Type.REGEX),
        /** */
        CULTURE_MAGIC (Type.REGEX),
        /** */
        MOVIE_MAGIC (Type.REGEX),
        /** */
        NEWS_MAGIC (Type.REGEX),
        /** */
        OCEAN_MAGIC (Type.REGEX),
        /** */
        PICTURE_MAGIC (Type.REGEX),
        /** */
        VIDEO_MAGIC (Type.REGEX),
        /** */
        RECEIPE_MAGIC (Type.REGEX),
        /** */
        SKIINFO_MAGIC (Type.REGEX),
        /** */
        STOCK_MAGIC (Type.REGEX),
        /** */
        TV_MAGIC (Type.REGEX),
        /** */
        WEATHER_MAGIC (Type.REGEX),
        /** */
        WEBTV_MAGIC (Type.REGEX),
        /** */
        WHITE_MAGIC (Type.REGEX),
        /** */
        WIKIPEDIA_MAGIC (Type.REGEX),
        /** */
        YELLOW_MAGIC (Type.REGEX),
        /** */
        MAP_MAGIC(Type.REGEX),
        /** */
        BLOG_MAGIC (Type.REGEX),

        // RegExp TokenPredicates -- trigger words/phrases
        CATALOGUE_TRIGGER (Type.REGEX),
        /** */
        CLASSIFIED_TRIGGER (Type.FAST),
        /** */
        LOAN_TRIGGER (Type.REGEX),
        /** */
        NEWS_TRIGGER (Type.REGEX),
        /** */
        OCEAN_TRIGGER(Type.REGEX),
        /** */
        PICTURE_TRIGGER (Type.REGEX),
        /** */
        VIDEO_TRIGGER (Type.REGEX),
        /** */
        SKIINFO_TRIGGER (Type.REGEX),
        /** */
        SUDOKU_TRIGGER (Type.REGEX),
        /** */
        TV_TRIGGER (Type.REGEX),
        /** */
        WEATHER_TRIGGER (Type.REGEX),
        /** */
        /** */
        WIKIPEDIA_TRIGGER (Type.REGEX),

        // RegExp TokenPredicates -- prefixes
        /** */
        SITEPREFIX (Type.REGEX),

        // RegExp TokenPredicates -- suffixes
        /** */
        COMPANYSUFFIX (Type.REGEX),

        // RegExp TokenPredicates -- general expression
        /** */
        ORGNR (Type.REGEX),
        /** */
        PHONENUMBER (Type.REGEX),
        /** */
        ONLYSKIINFO (Type.REGEX),
        /** */
        EMPTYQUERY (Type.REGEX),

        /** JepTokenPredicate. **/
        MATHPREDICATE (Type.JEP);

        // implementation to delegate to
        private final TokenPredicateImpl impl;

        /**
         * Create a new TokenPredicate that will return true if it applies to the
         * query.
         *
         * @param type     the fastListName type.
         */
        private Categories(final Type type) {

            this.impl = new TokenPredicateImpl(name(), type);
            // replace impl's entry with myself
            TokenPredicateImpl.TOKENS.remove(impl);
            TokenPredicateImpl.TOKENS_BY_TYPE.get(type).remove(impl);
            TokenPredicateImpl.TOKENS.add(this);
            TokenPredicateImpl.TOKENS_BY_TYPE.get(type).add(this);
        }

        public Type getType(){
            return impl.getType();
        }

        public boolean evaluate(final Object evalFactory) {

            return TokenPredicateImpl.evaluate(this, evalFactory);
        }

        public TokenPredicate exactPeer() {

            return impl.exactPeer();
        }



    }

    /** The default implementation used. Should not be used directly.
     * The Categories enumerations delegated to this class.
     * The anonymous TokenPredicates are instances of this class.
     */
    static class TokenPredicateImpl implements TokenPredicate{

        // Constants -----------------------------------------------------

        private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY
                = "Argument to evaluate must be an instance of a TokenEvaluationEngine";

        private static final Set<TokenPredicate> TOKENS
                = new CopyOnWriteArraySet<TokenPredicate>();
        private static final Map<Type,Set<TokenPredicate>> TOKENS_BY_TYPE
                = new ConcurrentHashMap<Type,Set<TokenPredicate>>();

        /** @deprecated todo take out of sesat. **/
        private static final Set<TokenPredicate> MAGIC_TOKENS = new CopyOnWriteArraySet<TokenPredicate>();
        /** @deprecated todo take out of sesat. **/
        private static final Set<TokenPredicate> TRIGGER_TOKENS = new CopyOnWriteArraySet<TokenPredicate>();

        // Attributes -----------------------------------------------------

        private final String name;
        private final Type type;
        private final ExactTokenPredicateImpl exactPeer;

        // Constructors -----------------------------------------------------

        private TokenPredicateImpl(final String name, final Type type){

            this.name = name;
            this.type = type;


            // TODO take out of sesat
            if(Type.REGEX == type){
                if(name.endsWith("_MAGIC")){
                    MAGIC_TOKENS.add(this);

                }else if(name.endsWith("_TRIGGER")){
                    TRIGGER_TOKENS.add(this);
                }
            }//end-take out of sesat

            TOKENS.add(this);
            TOKENS_BY_TYPE.get(type).add(this);

            exactPeer = new ExactTokenPredicateImpl(this);
        }


        // public -----------------------------------------------------

        @Override
        public String toString() {
            return name();
        }

        // TokenPredicate implementation ------------------------------------

        public String name(){
            return name;
        }

        public Type getType(){
            return type;
        }

        public boolean evaluate(final Object evalFactory) {

            return TokenPredicateImpl.evaluate(this, evalFactory);
        }

        public TokenPredicate exactPeer() {
            return exactPeer;
        }

        // private -----------------------------------------------------

        private static boolean evaluate(final TokenPredicate token, final Object evalFactory) {

            // pre-condition checks
            if (! (evalFactory instanceof TokenEvaluationEngine)) {
                throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
            }

            return ((TokenEvaluationEngine)evalFactory).evaluate(token);
        }

    }

    /**
     * An token predicate peer that only evaluates to true against its original token predicate on exact query matches.
     */
    static class ExactTokenPredicateImpl implements TokenPredicate{

        // Constants -----------------------------------------------------

        private static final String EXACT_PREFIX = "EXACT_";

        // Attributes -----------------------------------------------------

        private final TokenPredicate delegate;

        // Constructors -----------------------------------------------------

        private ExactTokenPredicateImpl(final TokenPredicate token){
            delegate = token;
        }

        // TokenPredicate implementation ------------------------------------

        public String name() {
            return EXACT_PREFIX + delegate.name();
        }

        public Type getType() {
            return delegate.getType();
        }

        public boolean evaluate(Object evalFactory) {

            final TokenEvaluationEngine engine = (TokenEvaluationEngine)evalFactory;
            final TokenEvaluationEngine.State originalState = engine.getState();
            try{

                engine.setState(originalState.getQuery().getEvaluationState());
                return engine.evaluate(this);

            }finally{
                engine.setState(originalState);
            }
        }

        public TokenPredicate exactPeer() {
            return this;
        }

        // private -----------------------------------------------------
    }

    /** Runtime exception thrown when evaluation fails.
     */
    static final class EvaluationException extends RuntimeException{
        public EvaluationException(final VeryFastListQueryException vflqe){
            super(vflqe);
        }
    }


    /** Utility class providing all useful static methods around TokenPredicates.
     * @todo move out to TokenPredicateUtility. **/
    static final class Static{

        private static final Map<String,TokenPredicate> ANONYMOUS_TOKENS = new ConcurrentHashMap<String,TokenPredicate>();

        static{
            // ensures all the enums have been loaded before any of the following static methods are called.
            // offspin to this is that there can be no references back to Static from Categories or TokenPredicateImpl.
            Categories.values();
        }

        /** Find a TokenPredicate that's already created.
         *
         * @param name the name of the TokenPredicate to find.
         * @return the TokenPredicate.
         * @throws IllegalArgumentException when no such anonymous token by the name exists.
         */
        public static TokenPredicate getTokenPredicate(final String name) throws IllegalArgumentException{

            try{
                return Categories.valueOf(name);
            }catch(IllegalArgumentException iae){
                return  getAnonymousTokenPredicate(name);
            }
        }

        /** Find a anonymous TokenPredicate that's already created.
         *
         * @param name the name of the TokenPredicate to find.
         * @return the anonymous TokenPredicate.
         * @throws IllegalArgumentException when no such anonymous token by the name exists.
         */
        public static TokenPredicate getAnonymousTokenPredicate(final String name) throws IllegalArgumentException{

            if(!ANONYMOUS_TOKENS.containsKey(name)){
                throw new IllegalArgumentException("No anonymous token found with name " + name);
            }
            return ANONYMOUS_TOKENS.get(name);
        }

        /** Creates an anonymous TokenPredicate.
         * Ensure the name doesn't clash with anonymous TokenPredicates from other skins.
         * Existing anonymous TokenPredicate with the same name will be replaced.
         *
         * @param name the TokenPredicate name
         * @param type the TokenPredicate type
         * @return the newly created anonymous TokenPredicate
         */
        public static TokenPredicate createAnonymousTokenPredicate(final String name, final Type type){

            ANONYMOUS_TOKENS.put(name, new TokenPredicateImpl(name, type));
            return getAnonymousTokenPredicate(name);
        }

        /** Utility method to use all TokenPredicates in existence.
         * @return set of all TokenPredicates. will not return instances that are delegates for Categories.
         */
        public static Set<TokenPredicate> getTokenPredicates() {

            return Collections.unmodifiableSet(TokenPredicateImpl.TOKENS);
        }

        /** Utility method to use all TokenPredicates belonging to a given type.
         * @param type the given type
         * @return set of all TokenPredicates with given type.
         */
        public static Set<TokenPredicate> getTokenPredicates(final Type type) {
            return Collections.unmodifiableSet(TokenPredicateImpl.TOKENS_BY_TYPE.get(type));
        }

        /** Utility method to use all MagicTokenPredicates in existence.
         * @return set of all MagicTokenPredicates
         * @deprecated to be moved out of sesat
         */
        public static Set<TokenPredicate> getMagicTokenPredicates() {
            return Collections.unmodifiableSet(TokenPredicateImpl.MAGIC_TOKENS);
        }

        /** Utility method to use all TriggerTokenPredicates in existence.
         * @return set of all TriggerTokenPredicates
         * @deprecated to be moved out of sesat
         */
        public static Set<TokenPredicate> getTriggerTokenPredicates() {
            return Collections.unmodifiableSet(TokenPredicateImpl.TRIGGER_TOKENS);
        }
    }

}