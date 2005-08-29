/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.configuration;

import java.io.IOException;
import java.util.Properties;

import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * A Constants.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class FastSearchConfigurationImpl implements FastSearchConfiguration {

    Logger log = Logger.getLogger(this.getClass());

	//variables that you may override when doing a search using a SearchConfiguration.
	private String qRServerURL = "http://localhost:15100"; 		//default overriden by QR_SERVER_PROPERTIES file
	private String template;									// Velocity Template
	private String language = "no"; 							//default, may be overriden by QR_SERVER_PROPERTIES file
	private String collection;
	private String collectionFilterString = "";					//"+meta.collection:";   //default, may be overriden by QR_SERVER_PROPERTIES file
	private String navigatorString = "";
    private boolean spellcheck = false; 							//default, may be overriden by QR_SERVER_PROPERTIES file

	//property file loaded on instanciation only
	private static Properties properties; 
	
	String query ="";
	long maxTime = 0L;
	int docsToReturn;
	int offSet;
    private String categoryModifier;


	/**
	 * Create a new SearchConfiguration.
	 *
	 * @param index
	 */
	public FastSearchConfigurationImpl() {

		//TODO: refactor reading properties into singleton pattern...?
		if(properties == null) {

			log.debug("Loading Fast property file");

			// set up the search engine from property file QR_SERVER_PROPERTIES
			//TODO: refactor to singleton pattern
	        try {

				properties = new Properties();
				properties.load(this.getClass().getResourceAsStream("/" + SearchConstants.FAST_PROPERTYFILE));

			} catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException("Unable to load configuration properties file: " + SearchConstants.FAST_PROPERTYFILE);
			}
		}

		//set up defaults for qrserver, language etc.
		if(!"".equals(properties.getProperty(SearchConstants.PROPERTY_KEY___QR_SERVER)))
            qRServerURL = properties.getProperty(SearchConstants.PROPERTY_KEY___QR_SERVER);
        if(!"".equals(properties.getProperty(SearchConstants._LANGUAGE)))
            language = properties.getProperty(SearchConstants._LANGUAGE);
        if(!"".equals(properties.getProperty(SearchConstants._SPELLCHECK)))
            spellcheck = Boolean.valueOf(properties.getProperty(SearchConstants._SPELLCHECK)).booleanValue();
	}

	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
		

	public boolean isSpellcheck() {
		return spellcheck;
	}
	

	public void setSpellcheck(boolean spellcheck) {
		this.spellcheck = spellcheck;
	}

    public void setCategoryModifer(String modifierName) {
        this.categoryModifier = modifierName;
    }

    public String getCategoryModifer() {
        return categoryModifier;
    }

	/**
	 *
	 * Create a +meta.collection filter based on which collection we are looking at.
	 *
	 * @param
	 * @return
	 */
	public String constructCollectionFilter() {

		String filterString = "";
		if(SearchConstants.WEBCRAWL_COLLECTION.equals(collection))
			filterString = "+meta.collection:" + SearchConstants.WEBCRAWL_COLLECTION;
		else if(SearchConstants.MEDIA_COLLECTION.equals(collection))
			filterString = "+meta.collection:" + SearchConstants.MEDIA_COLLECTION;
		else if(SearchConstants.WIKI_COLLECTION.equals(collection))
			filterString = "+meta.collection:" + SearchConstants.WIKI_COLLECTION;
        else if (SearchConstants.COMPANIES_COLLECTION.equals(collection))
            filterString = "+meta.collection:" + SearchConstants.COMPANIES_COLLECTION;
        else if (SearchConstants.PERSONS_COLLECTION.equals(collection))
            filterString = "+meta.collection:" + SearchConstants.PERSONS_COLLECTION;
        else if(SearchConstants.DEFAULTCOLLECTION.equals(collection))
			filterString = "";	//forces a collection wide search

//		if(log.isDebugEnabled())
//			log.debug("FILTER: " + filterString);
//
//		try {
//			filterString = URLEncoder.encode(filterString, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			log.error("Error: Unable to encode filterString " + filterString + " ", e);
//		}

		return filterString;

	}

	public String getQRServerURL() {
		return qRServerURL;
	}
	

	public void setQRServerURL(String serverURL) {
		qRServerURL = serverURL;
	}

	public String getCollection() {
		return collection;
	}
	

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getNavigatorString() {
		return navigatorString;
		
	}
	

	public void setNavigatorString(String navigatorString) {
		this.navigatorString = navigatorString;
	}

	public int getDocsToReturn() {
		return docsToReturn;
	}
	

	public void setDocsToReturn(int docsToReturn) {
		this.docsToReturn = docsToReturn;
	}
	

	public long getMaxTime() {
		return maxTime;
	}
	

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}
	

	public int getOffSet() {
		return offSet;
	}
	

	public void setOffSet(int offSet) {
		this.offSet = offSet;
	}
	

	public String getQuery() {
		return query;
	}
	

	public void setQuery(String query) {
		this.query = query;
	}

	public String getTemplate() {
		return template;
	}
	

	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("offSet", this.offSet).append(
				"docsToReturn", this.docsToReturn).append("spellcheck",
				this.spellcheck).append("template", this.template).append(
				"query", this.query).append("maxTime", this.maxTime).append(
				"navigatorString", this.navigatorString).append("QRServerURL",
				this.getQRServerURL()).append("language", this.language)
				.append("collection", this.collection).toString();
	}
	
	
	
	

}
