/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * A BaseSearchConfiguration.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class BaseSearchConfiguration implements SearchConfiguration {

	private String template;								// Velocity Template
	private String language = "en"; 							//default, may be overriden by QR_SERVER_PROPERTIES file
	String query ="";
	
	public String getTemplate() {
		return this.template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}

}
