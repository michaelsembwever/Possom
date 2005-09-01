/**
 * 
 */
package no.schibstedsok.front.searchportal.response;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.soap.search.GoogleSearchResultElement;
import com.yahoo.search.WebSearchResult;

/**
 * A SearchResultElement.
 * 
 * Abstract class overridden by concrete implementations.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public abstract class SearchResultElement implements Comparable {

    private String directoryCategory;
    private boolean relatedInformationPresent;
    private String title;           //
    private String summary;         //
    private String url;             // actual URL
    private String clickUrl;        // URL to show user
    private String contenttype;     //
    private String language;        // lang codes (comma-separated)
    private String charset;         // UTF-8/ISO-8859-1 etc.
    private String site;            // site only e.g www.schibsted.com
    private String crawltime;       // when documnet was fetched
    private String docdatetime;     // when document was updated on remote site
    private String size;            // target document size
    private String ranking;         // from systems supporting ranking of results

    /**
     * Default constructor
     */
    public SearchResultElement() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	
	/**
	 * Transfer Google response elements to our response 
	 * @param element
	 */
	public SearchResultElement(GoogleSearchResultElement element) {
        super();
        this.directoryCategory = element.getDirectoryCategory().getFullViewableName();
        this.relatedInformationPresent = element.getRelatedInformationPresent();
        this.summary = element.getSummary();
        this.title = element.getTitle();
        this.url = element.getURL();
        this.clickUrl = element.getURL();
        this.ranking = "0";
        
    }

    /**
     * Transfer Yahoo response elements to our response 
     * @param yahooResult
     */
	public SearchResultElement(WebSearchResult yahooResult) {
        super();
        this.summary = yahooResult.getSummary();
        this.title = yahooResult.getTitle();
        this.url = yahooResult.getClickUrl();
        this.clickUrl = yahooResult.getClickUrl();
        this.contenttype = yahooResult.getMimeType();
        this.docdatetime = yahooResult.getModificationDate();
        this.ranking = "0";
    }

    /** 
     * Not implemented yet. 
     * 
     * @return DirectoryCategory for this element
     */
    public String getDirectoryCategory() {
        return directoryCategory;
    }
    

    public void setDirectoryCategory(String directoryCategory) {
        this.directoryCategory = directoryCategory;
    }
    

    /** 
     * 
     * Not implemented yet.
     * 
     * @return Returns true if there is related information present.
     */
    public boolean isRelatedInformationPresent() {
        return relatedInformationPresent;
    }
    

    public void setRelatedInformationPresent(boolean relatedInformationPresent) {
        this.relatedInformationPresent = relatedInformationPresent;
    }


    /** 
     * 
     * The summary field of result, used for snippet presentation.
     * 
     * @return Summary
     */
    public String getSummary() {
        return summary;
    }
    

    public void setSummary(String summary) {
        this.summary = summary;
    }
    

    /**
     * 
     * Title for document
     * 
     * @return Title
     */
    public String getTitle() {
        return title;
    }
    

    public void setTitle(String title) {
        this.title = title;
    }

    /** 
     * 
     * URL to the document.
     * 
     * @return URL String
     */
    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    /** 
     * 
     * Url to present the in view for user. 
     * 
     * @return
     */
    public String getClickUrl() {
        if (clickUrl == null) {
            return null;
        }

        if (clickUrl.length() > 80) {
            return clickUrl.substring(0, 79) + " ...";
        }

        return clickUrl;
    }
    

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("directoryCategory", this.directoryCategory).append(
                        "title", this.title).append("summary", this.summary)
                .append("relatedInformationPresent",
                        this.relatedInformationPresent).append("clickUrl",
                        this.clickUrl).append("url", this.url).append("\n").toString();
    }
    
    
    

}
