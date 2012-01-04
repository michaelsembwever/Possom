/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.util;

/**
 * A SearchConstants.
 * <p/>
 * Various constants in use in application.
 *
 * @deprecated constants should be defined in the class that is primarily responsible for it.
 *
 * @version $Id$
 */
public final class SearchConstants {



    public static final String PROPERTY_KEY___QR_SERVER = "qrserver";                //property key
    public static final String PROPERTY_KEY___TV_PATTERN = "tv-pattern";            //property key
    public static final String _COLLECTION_KEY = "default-collection";            //property file key
    public static final String _SPELLCHECK = "spellcheck";                            //property file key
    public static final String _LANGUAGE = "default-language";                        //property file key
    public static final String _WEBCRAWL_TEMPLATE = "webcrawl-template";
    public static final String PIPELINE = "processList";
    public static final String DEFAULTCOLLECTION = "search_all_collections";
    public static final String COUNTERNAVIGATOR = "get_doc_counts_only";
    public static final String COUNTERNAVIGATORSTRING = "contentsourcenavigator";
    public static final String WEBCRAWL_COLLECTION = "webcrawl";
    public static final String WEBCRAWL_COLLECTION_NAVIGATOR = "Norske nettsider";
    public static final String MEDIA_COLLECTION = "retriever";
    public static final String MEDIA_COLLECTION_NAVIGATOR = "Norske nyheter";
    public static final String WIKI_COLLECTION = "wikipedia";
    public static final String WIKI_COLLECTION_NAVIGATOR = "Wikipedia";
    public static final String REQUEST_PARAM_COMPANIES_INDEX = "y";
    public static final String COMPANIES_COLLECTION = "yellow";
    public static final String COMPANIES_COLLECTION_NAVIGATOR = "Gule sider";
    public static final String BASE_PIPELINE = "standardPipeline";

    public static final String DEFAULT_LANGUAGE = "no";
    public static final String LANGUAGE_ENGLISH = "en";

    public static final String REQUEST_KEYPARAM_QUERY = "q";
    public static final String REQUEST_KEYPARAM_LANGUAGE = "lan";

    public static final String REQUEST_KEYPARAM_DOCUMENTS_TO_RETURN = "d";

    public static final Object REQUEST_PARAM_WIKICOLLECTION = "wiki";

    public static final String STD_CONTENT_TYPE = "text/html";

    public static final String FAST_COMPANY_ID_FIELD = "recordid";
    public static final String PERSONS_COLLECTION = "white";

    public static final Object REQUEST_PARAM_PERSONS_INDEX = "w";
    public static final String MOREOVER_COLLECTION = "moreover";
    public static final String PERSON_ID_FIELD = "personId";
    public static final String NORDIC_NEWS_COLLECTION = "retrievernordic";
    public static final String PIC_SEARCH_HOST = "license.picsearch.com";
    public static final String WEATHER_SEARCH_HOST = "www.storm.no/kunder/schibsted";

}



