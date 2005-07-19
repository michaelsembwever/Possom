/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.response;

/**
 * A GoogleSearchResult.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class GoogleSearchResultElement extends SearchResultElement {

	/**
	 * Create a new GoogleSearchResultElement.
	 * 
	 * @param element
	 */
	public GoogleSearchResultElement(com.google.soap.search.GoogleSearchResultElement element) {
		// FIXME GoogleSearchResultElement constructor
		super(element);
	}

	public int compareTo(Object o) {
		// FIXME compareTo
		return 0;
	}


}
