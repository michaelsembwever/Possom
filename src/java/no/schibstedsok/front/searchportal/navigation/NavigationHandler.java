package no.schibstedsok.front.searchportal.navigation;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchTabs;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;


/**
 * NavigationHandler.
 * 
 * Holds state regarding navigation graphs and their nodes. 
 *  
 * @author Lars Johansson
 *
 */
public class NavigationHandler implements Serializable {

	/**
	 * Unique id for handler
	 */ 
//	private String handlerId = IdGenerator.getInstance().generateUniqueId();
	
	private String collection;
	private String query;		//navigation is only valid within a single query
	private List graphs = new ArrayList();

	private NavigationHandler(String query, String collection, Collection graphs) {
		this.query = query;
		this.collection = collection;
		this.graphs = (List)graphs;
	}

	/**
	 * The collection name for this handler.
	 * @return
	 */
	public String getCollection() {
		return collection;
	}

	/**
	 * Create a new NavigationHandler.
	 * @param collection
	 * @param query2 
	 * @return
	 */
	public static NavigationHandler getNewHandler(String collection, String query) {
		
		SearchMode searchMode = getSearchMode(collection);

		//TODO: get the first Configuration? 
		FastConfiguration fastConfiguration = (FastConfiguration) (searchMode.getSearchConfigurations().iterator().next());

		Collection navigatorHierarchy  = new ArrayList();
		Collection graphs = new ArrayList();

		for (Iterator iterator = fastConfiguration.getNavigators().keySet().iterator(); iterator.hasNext();) {
			String key = (String)iterator.next();
			FastNavigator navigator = (FastNavigator) fastConfiguration.getNavigators().get(key);
			navigatorHierarchy = flattenNavigators(navigatorHierarchy, navigator);
			graphs.add(new NavigationGraph(navigator.getDisplayName(), navigatorHierarchy));
			navigatorHierarchy = new ArrayList();
		}
			
		return new NavigationHandler(query, collection, graphs);
	}


	/**
	 * Utility to get the SearchMode for specific collection.
	 * 
	 * @param collection
	 * @return
	 */
	private static SearchMode getSearchMode(String collection) {
		SearchTabs tabs = XMLSearchTabsCreator.getInstance().createSearchTabs();
		return tabs.getSearchMode(collection);
	}
	
	/**
	 * Flaaten out the navigators stored in the configuration in descending order. 
	 * 
	 * @param soFar
	 * @param nav
	 * @return Navigators
	 */
	private static synchronized Collection flattenNavigators(Collection soFar, FastNavigator nav) {
		
		soFar.add(nav);

		if (nav.getChildNavigator() != null) {
			flattenNavigators(soFar, nav.getChildNavigator());
		}

		return soFar;
	}

	/**
	 * Get all graphs (navigators) for the NavigationHandler.
	 * @return
	 */
	public List getGraphs() {
		return graphs;
	}

	/**
	 * Serializes the NavigationHandler with graphs and nodes. Use
	 * when you wish to send the graphs state over the wire (request).
	 * 
	 * @return The object as byte[]
	 */
	public byte[] serialize() {
		
		try {     
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2500);
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			os.writeObject(this);
			os.flush();
			return  byteStream.toByteArray();
			
	    } catch (IOException ex){
	    	ex.printStackTrace();
	    }
	    return null;
		
	}

//	/**
//	 * 
//	 * Id used for store and retrieval.
//	 * @return
//	 */
//	public String getHandlerId() {
//		return handlerId;
//	}
//	/**
//	 * 
//	 * @param handlerId
//	 */
//	public void setHandlerId(String handlerId) {
//		this.handlerId = handlerId;
//	}
	/**
	 * @deprecated Not in use.
	 * @return
	 */
	public void persist() {
		NavigationHandlerStore.getInstance().add(this);
//		return this.handlerId;
	}

	public NavigationGraph getGraph(String selectedGraph) {
		if(graphs.size() > 0)
			return (NavigationGraph)graphs.get(graphs.indexOf(new NavigationGraph(selectedGraph, null)));
		else 
			return null;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}


}
