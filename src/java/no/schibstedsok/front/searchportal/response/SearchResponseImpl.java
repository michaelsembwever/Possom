/**
 * 
 */
package no.schibstedsok.front.searchportal.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A SearchResponseImpl.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SearchResponseImpl implements CommandResponse {

    private long fetchTime; // how long the query took

	private String searchErrorMesg; // if any error occured

    private int documentsReturned; // number of results 

    private int totalDocumentsAvailable;

    private Collection results = new ArrayList();

    private int consequtiveSearchStartsAt;
	
	private String query;

	//for back/forward operations in resultset
	private int[] previousSet;
	private int[] nextSet;
	private int currentPostionInSet;

    /**
     * Default empty Constructor
     */
    public SearchResponseImpl() {
        super();
    }


	/**
	 * 
	 *  Add a result to the list. 
	 * @param result
	 * 
	 */
	public void addResult(SearchResultElement result){
		results.add(result);
	}

	/**
	 * Get <code>Collection</code> of <code>SearchResultElement</code>
	 * 
	 * @return <code>Collection</code>
	 */
	public Collection getResults() {
        return this.results;
    }

    /**
     *
     * Set complete list of <code>SearchResult</code> wrapped
     * in <code>Collection</code>
     */
    public void setResults(Collection result) {
        this.results = (List) result;
    }

    /**
     *
     * Get number of documents returned from Fast
     * 
     */
    public int getDocumentsReturned() {
        return documentsReturned;
    }

    /**
     * 
     * Set number of documents returned from Fast
     * 
     * @param documentsReturned from Fast 
     */
    public void setDocumentsReturned(int documentsReturned) {
        this.documentsReturned = documentsReturned;
    }

    /**
     * Get how long the query took
     * 
     * @return long time in millisec.
     */
    public long getFetchTime() {
        return fetchTime;
    }

    /**
     * Set query execution time in millisec.
     * 
     *  @param fetchTime in millisec
     */
	public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

	
    /**
     * Get how many documents available in index in total.
     * 
     * @return total Documents available 
     */
    public int getTotalDocumentsAvailable() {
        return totalDocumentsAvailable;
    }

    /**
     * 
     * Set total docuents available in index. 
     */
	public void setTotalDocumentsAvailable(int totalDocumentsAvailable) {
        this.totalDocumentsAvailable = totalDocumentsAvailable;
    }

    /**
     * 
     * Get a pointer to where in resultset we are currently.
     * Use for next search to inidicate offset in index.
     * 
     * @return the offset in index we are at.
     *  
     */
	public int getConsequtiveSearchStartsAt() {
        return consequtiveSearchStartsAt;
    }

    /**
     * Set offset in index.
     */
	public void setConsequtiveSearchStartsAt(int consequtiveSearchStartsAt) {
        this.consequtiveSearchStartsAt = consequtiveSearchStartsAt;
    }

	/** 
	 * see if there is more documents available based on documents returned 
	 * */
	public boolean hasMoreDocuments(){
		return totalDocumentsAvailable > documentsReturned;
	}


	/**
	 * 
	 *  The query that was passed and executed.
	 * 
	 * @return the queryString
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * 
	 * Set query string.
	 * 
	 * @param query The query.
	 */
	public void setQuery(String query) {
		this.query = query;
	}


	public int getCurrentPostionInSet() {
		return currentPostionInSet;
	}
	


	public void setCurrentPostionInSet(int currentPostionInSet) {
		this.currentPostionInSet = currentPostionInSet;
	}
	


	public int[] getNextSet() {
		return nextSet;
	}
	


	public void setNextSet(int[] nextSet) {
		this.nextSet = nextSet;
	}
	


	public int[] getPreviousSet() {
		return previousSet;
	}
	


	public void setPreviousSet(int[] previousSet) {
		this.previousSet = previousSet;
	}


	public String getSearchErrorMesg() {
		return searchErrorMesg;
	}
	


	public void setSearchErrorMesg(String searchErrorMesg) {
		this.searchErrorMesg = searchErrorMesg;
	}
	
	

}
