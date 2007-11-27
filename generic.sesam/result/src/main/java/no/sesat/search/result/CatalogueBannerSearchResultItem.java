/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.result;

/**
 * Result object holding one banner that should be displayed in the 
 * catalogue search result page.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Id$</tt>
 */
public final class CatalogueBannerSearchResultItem extends BasicResultItem {

	
	/**
	 * Constructor to initialize the object with default values.
	 * 
	 * @param keyword
	 * @param url
	 */
	public CatalogueBannerSearchResultItem(String keyword, String url, String homepage){
		addField("iypbannerkw4", keyword);
		addField("iypbannerkw4url", url);
		addField("iypurl", homepage);
	}
	
	/**
	 * The url to go to when cliking on the banner. 
	 * 
	 * @return
	 */
	public String getHomepage(){
		return getField("iypurl");
	}
	
	
	public void setHomepage(String s){
		addField("iypurl", s);
	}
	
	/**
	 * Get the banner url
	 * @return string representing a url.
	 */
	public String getBannerUrl(){
		return getField("iypbannerkw4url");
	}
	
	/**
	 * Set the banner url
	 * 
	 * @param s String that represents a url.
	 */
	public void setBannerUrl(String s){
		addField("iypbannerkw4url", s);
	}

	/**
	 * Set the banner keyword. 
	 * This is the keyword that the banner was registered on.
	 * 
	 * @param bannerKeyword
	 */
	public void setBannerKeyword(String bannerKeyword) {
		addField("iypbannerkw4",bannerKeyword);
	}

	/**
	 * Get the keyword that the banner was registered on.
	 * @return the keyword.
	 */
	public String getBannerKeyword() {
		return getField("iypbannerkw4");
	}
	
	
	/**
	 * To string method with a pretty output.
	 */
	public String toString(){
		return "CatalogueBannerSearchResultItem[bannerKeyword="+getBannerKeyword()+", bannerUrl="+ getBannerUrl()+"]";
	}
	
}