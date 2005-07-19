/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.util;

/**
 * A SearchConfiguration.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public interface SearchConfiguration {

	public String getLanguage();
	public void setLanguage(String language);

	public String getQuery();
	public void setQuery(String query);
	
	public String getTemplate();
	public void setTemplate(String string);

}
