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
    ANIMAL,
    CATEGORY,
    CELEBRITY,
    COMPANYBRANCH,
    COMPANYBRANCHKEYWORD,
    CLASSIFIED_CATEGORY,
    COMPANY_CHAIN,
    COMPANYENRICHMENT,
    COMPANY_KEYWORD,
    COMPANY_KEYWORD_RESERVED,
    BIGCOMPANY,
    DISEASE,
    ENGLISHWORDS,
    GEOLOCAL,
    GEOGLOBAL,
    GEO_BOROUGH,
    GEO_COUNTY,
    GEO_STREET,
    GEO_MUNICIPALITY,
    GEO_AREA,
    GEO_ZIPCODE,
    GEO_POSTALPLACE,
    FIRSTNAME,
    FOOD,
    FULLNAME,
    LASTNAME,
    MATERIAL,
    MOVIE_TITLE,
    MOVIE_ACTOR,
    MOVIE_DIRECTOR,
    NEWSCASE,
    NOPICTURE,
    PICTURE,
    PRIOCOMPANYNAME,
    PRODUCT_BICYCLE,
    PRODUCT_CAR,
    PRODUCT_CHILDREN,
    PRODUCT_CLOTHING,
    PRODUCT_CONSTRUCTION,
    PRODUCT_COSTUME,
    PRODUCT_ELECTRONIC,
    PRODUCT_FURNITURE,
    PRODUCT_GARDEN,
    PRODUCT_HOBBY,
    PRODUCT_HOUSEHOLD,
    PRODUCT_JEWELRY,
    PRODUCT_MOTOR,
    PRODUCT_MUSIC,
    PRODUCT_SHOE,
    PRODUCT_SPORT,
    PRODUCT_WATCH,
    PRODUCT_WEAPON,
    PRODUCT_TORGET,
    PROFESSION,
    OCEAN,
    STOCKMARKETTICKERS,
    STOCKMARKETFIRMS,
    STYLE,
    TNS,
    TVPROGRAM,
    TVCHANNEL,
    TRADEMARK,
    WIKIPEDIA,
    ARTIST,
    FICTION_CHARACTER,
    MOTOR_SPORT,
    PUBLIC_SERVICE_BROADCASTING,
    IMAGES,
    BOOK_MAGIC,
    CATALOGUE_MAGIC,
    CLASSIFIED_MAGIC,
    CULTURE_MAGIC,
    MOVIE_MAGIC,
    NEWS_MAGIC,
    OCEAN_MAGIC,
    PICTURE_MAGIC,
    VIDEO_MAGIC,
    RECEIPE_MAGIC,
    SKIINFO_MAGIC,
    STOCK_MAGIC,
    TV_MAGIC,
    WEATHER_MAGIC,
    WEBTV_MAGIC,
    WHITE_MAGIC,
    WIKIPEDIA_MAGIC,
    YELLOW_MAGIC,
    MAP_MAGIC,
    BLOG_MAGIC,
    CATALOGUE_TRIGGER,
    CLASSIFIED_TRIGGER,
    LOAN_TRIGGER,
    NEWS_TRIGGER,
    OCEAN_TRIGGER,
    PICTURE_TRIGGER,
    VIDEO_TRIGGER,
    SKIINFO_TRIGGER,
    SUDOKU_TRIGGER,
    TV_TRIGGER,
    WEATHER_TRIGGER,
    WIKIPEDIA_TRIGGER,
    SITEPREFIX,
    COMPANYSUFFIX,
    ORGNR,
    PHONENUMBER,
    ONLYSKIINFO,
    EMPTYQUERY,
    MATHPREDICATE;

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
