/* Copyright (2008) Schibsted Søk AS
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

/**
 * A formalised breakdown of metadata categories that search terms can match.
 *
 * The break down of these categories should roughly follow what is found at
 * http://en.wikipedia.org/wiki/Portal:Contents/Categorical_index
 *
 * @version $Id$
 */
public enum Categories implements TokenPredicate {

    /**
     * {@link http://en.wikipedia.org/wiki/Category:Animals}
     */
    ANIMAL,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Products_by_company} ???
     * @deprecated use COMPANYBRANCH instead
     */
    CATEGORY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Celebrities}
     */
    CELEBRITY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Products_by_company}
     */
    COMPANYBRANCH,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Products_by_company}
     * @deprecated a prioritised category of COMPANYBRANCH is business logic
     */
    COMPANYBRANCHKEYWORD,
    /** Advertising categories, typically represented by "classifieds" websites.
     * {@link http://en.wikipedia.org/wiki/Category:Advertising}
     */
    CLASSIFIED_CATEGORY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Distribution,_retailing,_and_wholesaling}
     * @todo rename to CHAIN_STORE
     */
    COMPANY_CHAIN,
    /** prioritised category of COMPANYNAME
     * @deprecated a prioritised category of COMPANYNAME is business logic
     */
    COMPANYENRICHMENT,
    /** a prioritised category of COMPANYBRANCH
     * @deprecated a prioritised category of COMPANYBRANCH is business logic
     */
    COMPANY_KEYWORD,
    /** a prioritised category of COMPANYBRANCH
     * @deprecated a prioritised category of COMPANYBRANCH is business logic
     */
    COMPANY_KEYWORD_RESERVED,
    /** a prioritised category of COMPANYNAME
     * @deprecated a prioritised category of COMPANYNAME is business logic
     */
    BIGCOMPANY,
    /**
     * {@ link http://en.wikipedia.org/wiki/Category:Diseases}
     */
    DISEASE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:English_language}
     */
    ENGLISHWORDS,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Geography}
     * National geographical places (according to current skin).
     *
     */
    GEOLOCAL,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Geography}
     * International geographical places.
     */
    GEOGLOBAL,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Boroughs}
     */
    GEO_BOROUGH,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Counties}
     */
    GEO_COUNTY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Streets_and_roads}
     */
    GEO_STREET,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Municipalities}
     */
    GEO_MUNICIPALITY,
    /**
     * @deprecated ambiguos (within a geographical context). Essentially it is a mathematical term.
     * Will be removed before Sesat-3.0
     */
    GEO_AREA,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Postal_codes_by_country}
     * @todo Will be renamed to GEO_POSTALCODE as ZIPCODE is an American name.
     */
    GEO_ZIPCODE,
    /**
     * @deprecated  ambiguous. what is a postal place?
     * somewhere that can receive post?
     */
    GEO_POSTALPLACE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Given_names}
     * @todo rename to GIVEN_NAME as the position of the given name is cultural.
     */
    FIRSTNAME,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Food_and_drink}
     */
    FOOD,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Names}
     * @todo rename to NAME
     */
    FULLNAME,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Surnames}
     * @todo rename to SURNAME as the position of the given name is cultural.
     */
    LASTNAME,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Materials}
     */
    MATERIAL,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Films}
     */
    MOVIE_TITLE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Actors}
     * @todo rename to ACTOR as they are not restricted to film.
     */
    MOVIE_ACTOR,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Film_directors}
     */
    MOVIE_DIRECTOR,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:News}
     * @todo rename to NEWS
     */
    NEWSCASE,
    /**
     * @deprecated will be removed
     * before sesat-3.0
     */
    NOPICTURE,
    /**
     * @deprecated will be removed before sesat-3.0.
     * what words to show a visual image of is business logic.
     */
    PICTURE,
    /**  a Prioritised Company
     * @deprecated the definition of a Prioritised Company is business logic.
     */
    PRIOCOMPANYNAME,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Cycle_types}
     * @todo rename to BICYCLE
     */
    PRODUCT_BICYCLE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Automobiles}
     * @todo rename to AUTOMOBILE
     */
    PRODUCT_CAR,
    /**
     * Products typically for children.
     */
    PRODUCT_CHILDREN,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Clothing}
     */
    PRODUCT_CLOTHING,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Construction}
     * @rename to CONSTRUCTION as not _neccessarily_ related to "construction equipment"
     */
    PRODUCT_CONSTRUCTION,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Costume_design}
     * @todo rename to COSTUME
     */
    PRODUCT_COSTUME,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Electronics}
     * @todo rename to ELECTRONIC
     */
    PRODUCT_ELECTRONIC,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Furniture}
     * @todo rename to FURNITURE
     */
    PRODUCT_FURNITURE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Gardening}
     * @todo rename to GARDENING
     */
    PRODUCT_GARDEN,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Hobbies}
     * @todo rename to HOBBY
     */
    PRODUCT_HOBBY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Home}
     * @todo rename to HOME
     */
    PRODUCT_HOUSEHOLD,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Jewellery}
     * @todo rename to JEWELRY
     */
    PRODUCT_JEWELRY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Vehicles}
     * @todo rename to VECHICLE
     */
    PRODUCT_MOTOR,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Music}
     * @todo rename to MUSIC
     */
    PRODUCT_MUSIC,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Shoes}
     * @todo rename to SHOE
     */
    PRODUCT_SHOE,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Sports}
     * @todo rename to SPORT
     */
    PRODUCT_SPORT,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Watches}
     * @todo rename to WATCH
     */
    PRODUCT_WATCH,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Weapons}
     * @todo rename to WEAPON
     */
    PRODUCT_WEAPON,
    /**
     * @deprecated specific to finn.no
     * will be removed before sesat-3.0
     */
    PRODUCT_TORGET,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Occupations}
     * @todo rename to OCCUPATION
     */
    PROFESSION,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Bodies_of_water}
     */
    OCEAN,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Security_identifier_types}
     * @todo rename to STOCK_SYMBOL
     */
    STOCKMARKETTICKERS,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Publicly_traded_companies}
     * @todo rename to PUBLIC_COMPANY
     */
    STOCKMARKETFIRMS,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Genres}
     * @todo rename to GENRE
     */
    STYLE,
    /**
     * @deprecated will be remove before sesat-3.0.
     * a list of locally popular commercial websites is business related.
     */
    TNS,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Television_programming}
     */
    TVPROGRAM,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Television_stations}
     */
    TVCHANNEL,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Trademarks}
     */
    TRADEMARK,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Wikipedia}
     */
    WIKIPEDIA,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Artists}
     */
    ARTIST,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Fictional_characters}
     */
    FICTION_CHARACTER,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Motorsport}
     */
    MOTOR_SPORT,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Publicly_funded_broadcasters}
     * @deprecated will be removed before sesat-3.0. use anonymous or see #TVPROGRAM
     */
    PUBLIC_SERVICE_BROADCASTING,
    /**
     * @deprecated will be removed before sesat-3.0.
     * what words to show a visual image of is business logic.
     */
    IMAGES,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    BOOK_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    CATALOGUE_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    CLASSIFIED_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    CULTURE_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    MOVIE_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    NEWS_MAGIC,
    /**
     *
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    OCEAN_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    PICTURE_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    VIDEO_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    RECEIPE_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    SKIINFO_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    STOCK_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    TV_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    WEATHER_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    WEBTV_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    WHITE_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    WIKIPEDIA_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    YELLOW_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    MAP_MAGIC,
    /**
     * @deprecated not a category,
     * but a keyword used as a portal into another search mode
     */
    BLOG_MAGIC,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    CATALOGUE_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    CLASSIFIED_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    LOAN_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    NEWS_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    OCEAN_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    PICTURE_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    VIDEO_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    SKIINFO_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    SUDOKU_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    TV_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    WEATHER_TRIGGER,
    /**
     * @deprecated not a category,
     * but keywords used as a portal into another search mode
     */
    WIKIPEDIA_TRIGGER,
    /**
     * @deprecated inappropriate method to determine
     * if the "site:" field has been applied to a term.
     */
    SITEPREFIX,
    /** While not exactly matching the "Types of Companies" category
     * {@link http://en.wikipedia.org/wiki/Category:Types_of_companies}
     * it does intend to provide a list of abbreviations commonly used to identify
     * these difference types of companies
     */
    COMPANYSUFFIX,
    /**
     * Each country has a different format in its company/organisation numbers.
     * For example in norway organisation numbers are always nine digits.
     */
    ORGNR,
    /**
     * Each country has a different format for phone numbers.
     * The query parser has a very generic test for matching phone numbers that appear in any country.
     * This Category can be used to further test against a particular country's format.
     */
    PHONENUMBER,
    /**
     * @deprecated will be removed
     * before sesat-3.0
     */
    ONLYSKIINFO,
    /**
     * A completely empty query.
     */
    EMPTYQUERY,
    /**
     * {@link http://en.wikipedia.org/wiki/Category:Equations}
     */
    MATH;

    private final TokenPredicateImpl impl;

    private Categories() {
        this.impl = new TokenPredicateImpl(name());
        // replace impl's entry with myself
        TokenPredicateImpl.TOKENS.remove(impl);
        TokenPredicateImpl.TOKENS.add(this);
    }

    public boolean evaluate(final Object evalFactory) {

        return AbstractTokenPredicate.evaluate(this, evalFactory);
    }

    public TokenPredicate exactPeer() {

        return impl.exactPeer();
    }
}
