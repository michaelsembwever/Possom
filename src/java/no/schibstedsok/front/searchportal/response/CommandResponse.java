/**
 * 
 */
package no.schibstedsok.front.searchportal.response;

import java.util.Collection;

/**
 * @author Lars Johansson
 *
 */
public interface CommandResponse {

    public long getFetchTime();             	// how long the query took
    public void setFetchTime(long timeInMillis);             
    
    public int getDocumentsReturned();      	// number of results
    public void setDocumentsReturned(int documents);
    
    public int getTotalDocumentsAvailable();     // how many documents there is in search space in total
    public void setTotalDocumentsAvailable(int totalResultsAvailable);

    /** A collection of the specified type <i>SearchResult</i>...*/
    public Collection getResults();
    public void setResults(Collection values);

    public void setConsequtiveSearchStartsAt(int endIndex);     //where to start next search
    public int getConsequtiveSearchStartsAt();
	
	/**
	 * 
	 * Keep track of where in the searchReults
	 * we currently are located with these fields.
	 * 
	 */
	public int[] getPreviousSet();
	public int[] getNextSet();
	public int getCurrentPostionInSet();
		

	
}
