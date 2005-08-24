/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.util;


/**
 * A SearchConstants.
 * 
 * Various constants in use in application.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class SearchConstants {

	//Static properties file variables
    public static final String 	FAST_PROPERTYFILE ="fast.properties";				//the properties file for configuration of FAST search and templates 
    public static final String 	SENSIS_PROPERTYFILE = "sensis.properties";			//the properties file for configuration of FAST search and templates 
    public static final String 	PATTERNS_PROPERTYFILE = "text-patterns.properties";	//the properties file for textual patterns 
    public static final String 	PROPERTY_KEY___QR_SERVER = "qrserver";				//property key
    public static final String 	PROPERTY_KEY___TV_PATTERN = "tv-pattern";			//property key
    public static final String 	_COLLECTION_KEY  = "default-collection";			//property file key
	public static final String 	_SPELLCHECK = "spellcheck";							//property file key
	public static final String 	_LANGUAGE = "default-language";						//property file key
	public static final String 	_WEBCRAWL_TEMPLATE = "webcrawl-template";
	public static final String  PIPELINE = "processList";
	public static final String 	DEFAULTCOLLECTION = "search_all_collections";
	public static final String 	COUNTERNAVIGATOR = "get_doc_counts_only";
	public static final String 	COUNTERNAVIGATORSTRING = "contentsourcenavigator";
	public static final String 	WEBCRAWL_COLLECTION = "webcrawl";
	public static final String 	WEBCRAWL_COLLECTION_NAVIGATOR = "Norske nettsider";
	public static final String 	MEDIA_COLLECTION = "retriever";
	public static final String 	MEDIA_COLLECTION_NAVIGATOR = "Norske nyheter";
	public static final String 	WIKI_COLLECTION = "wikipedia";
	public static final String 	WIKI_COLLECTION_NAVIGATOR = "Wikipedia";
    public static final String  REQUEST_PARAM_COMPANIES_INDEX = "y";
    public static final String  COMPANIES_COLLECTION = "yellow";
    public static final String  COMPANIES_COLLECTION_NAVIGATOR = "Gule sider";
	public static final String 	BASE_PIPELINE = "standardPipeline";
	
	public static final String 	DEFAULT_LANGUAGE = "no";
	public static final String 	LANGUAGE_ENGLISH = "en";
	public static final int 	DEFAULT_DOCUMENTS_TO_RETURN= 10;
	
	public static final String 	REQUEST_KEYPARAM_QUERY = "q";
	public static final String 	REQUEST_KEYPARAM_LANGUAGE = "lan";
	public static final String 	REQUEST_KEYPARAM_OFFSET = "offset";
	public static final String 	REQUEST_KEYPARAM_DOCUMENTS_TO_RETURN = "d";
	public static final String 	REQUEST_KEYPARAM_COLLECTION = "c";
	public static final String 	REQUEST_PARAM_DEFAULT_COLLECTIONS = "d";
	public static final String 	REQUEST_PARAM_MEDIA_COLLECTIONS = "m";
	public static final String 	REQUEST_PARAM_GLOBAL_INDEX = "g";
	public static final Object 	REQUEST_PARAM_WIKICOLLECTION = "wiki";
	
	public static final String 	STD_CONTENT_TYPE = "text/html";

    public static final String FAST_COMPANY_ID_FIELD = "recordid";
    public static final String PERSONS_COLLECTION = "white";

    public static final Object REQUEST_PARAM_PERSONS_INDEX = "w";
}
