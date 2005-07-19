/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.response;

import com.yahoo.search.WebSearchResult;

/**
 * A YahooSearchResultElement.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class YahooSearchResultElement extends SearchResultElement {

	/**
	 * Create a new YahooSearchResultElement.
	 * 
	 * @param yahooResult
	 */
	public YahooSearchResultElement(WebSearchResult yahooResult) {
		// FIXME YahooSearchResultElement constructor
		super(yahooResult);
	}

	public int compareTo(Object o) {
		return 0;
	}

}
