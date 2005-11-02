package no.schibstedsok.front.searchportal.navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import no.fast.ds.search.IModifier;
import no.fast.ds.search.INavigator;
import no.fast.ds.search.IQueryResult;
import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NavigationGraph.
 * A graph is a stateful object holding information regarding 
 * where a User currently is located.
 * 
 * @author Lars Johansson
 */
public class NavigationGraph implements Serializable {

	private transient static Log log = LogFactory.getLog(FastSearchCommand.class);
	private String graphName;
	
	/**
	 * Each graph has a defined hierarchy defined in the search-tabs descriptor
	 */
	private List navigatorHierarchy = new ArrayList();
	
	/**
	 * The NavigationNode the user is currently located at.  
	 */
	NavigationNode currentPosition;
	private long lastThreadSelect;
	
	public NavigationGraph(String graphName, Collection navigatorHierarchy) {
		this.graphName = graphName;
//		this.searchConfiguration = configuration;
		this.navigatorHierarchy = (List)navigatorHierarchy;
		
		//set up root node with default empty filter 
		this.currentPosition = new RootNode();
		this.currentPosition.setName(graphName);
	}

	public List getHierarchy() {
		return navigatorHierarchy;
	}

	/**
	 * Based on the hierarchy, get a specific Navigator.
	 * 
	 * @param i
	 * @return The FastNavigator Name
	 */
	public FastNavigator getNavigator(int i) {
		if(navigatorHierarchy == null || navigatorHierarchy.size() <= i){
			return null;
		}
		FastNavigator nav = (FastNavigator)navigatorHierarchy.get(i);
		return nav;
	}
	
	/**
	 * Based on the hierarchy, get a specific Navigator.
	 * 
	 * @param i
	 * @return The FastNavigator Name
	 */
	public String getNavigatorName(int i) {
		if(navigatorHierarchy != null && navigatorHierarchy.size() <= i ) {
			return null;
		}
		FastNavigator nav = (FastNavigator)navigatorHierarchy.get(i);
		return nav.getName();
	}
	
	/**
	 * The current position the User is at.
	 * @return
	 */
	public NavigationNode getPosition() {
		return currentPosition;
	}

	public void up(String to) {
		
		if (currentPosition instanceof RootNode) {
			return;
		}
		
		NavigationNode node = currentPosition.getParent();
		up(to, node);
	}

	private void up(String to, NavigationNode node) {
		
		if (node instanceof RootNode) {
			currentPosition = node;
			return;
		}
		
		if(!node.getName().equals(to) || !node.isVisible()) {
			node = node.getParent();
			up(to, node);
		} else 
			currentPosition = node;	

	}
	

	/**
	 * Select a specific NavigationNode. This creates a new 
	 * NavigationNode with the selected "option" and updates 
	 * the current position in the graph. 
	 * 
	 * @param option
	 * @return
	 */
	public NavigationNode select(String option) {
		//this is a hack for the stange double selects!
//		if(lastThreadSelect != Thread.currentThread().getId()) {
//			this.lastThreadSelect = Thread.currentThread().getId();
//			log.debug("---------------------------------> " + this.graphName + " Selected in thread " + Thread.currentThread().getId() + "<---------------------------------------------------");
			NavigationNode node = new NavigationNode();
			node.setLevel(currentPosition.getLevel() + 1);	//this is wrong!
			FastNavigator navigator = getNavigator(currentPosition.getLevel());
			if(navigator != null) {
				node.setFilter(navigator.getField() + ":\"" + option + "\"");
				node.parent = currentPosition;
				node.setName(option); 
				currentPosition = node;
			}
//		}
		
		return currentPosition;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("NavigationGraph ");
		buf.append(this.graphName).append(", ")
			.append(currentPosition.toString()).append(" ").append(currentPosition.createFilter());
		return buf.toString();
	}

	public int compareTo(Object arg0) {
		return this.graphName.compareTo(arg0);
	}
	public int getCurrentLevel() {
		return getPosition().level;
	}

	public void updateNavigationGraph(IQueryResult result) {
		updateGraph(result, this.getCurrentLevel());
	}

	private void updateGraph(IQueryResult result, int level) {
		INavigator nav = result.getNavigator(this.getNavigatorName(level));
		
		
		if(nav != null) {

			Iterator modifiers = nav.modifiers();
			while (modifiers.hasNext()) {
				IModifier modifier = (IModifier) modifiers.next();
				this.getPosition().addOption(modifier.getName(), modifier.getValue(), modifier.getCount());
			}

			if(nav.modifierCount() == 1) {
				IModifier modifier = (IModifier)nav.modifiers().next();
				this.select(modifier.getValue());	//auto-select levels with one entry
//				System.out.println("********* FilterDebug Autoselected " + modifier.getAttribute() + " " + level);
				this.getPosition().setVisible(false);
				updateGraph(result, level + 1);
			} 
//			else {
//				Iterator modifiers = nav.modifiers();
//				while (modifiers.hasNext()) {
//					IModifier modifier = (IModifier) modifiers.next();
//					this.getPosition().addOption(modifier.getName(), modifier.getValue(), modifier.getCount());
//				}
//			}
		}
//INavigator nav = result.getNavigator(this.getNavigatorName(level));
//		if(nav != null) {
//			if(nav.modifierCount() == 1) {
//				IModifier modifier = (IModifier)nav.modifiers().next();
//				this.select(modifier.getValue());	//auto-select levels with one entry
////				System.out.println("********* FilterDebug Autoselected " + modifier.getAttribute() + " " + level);
//				this.getPosition().setVisible(false);
//				updateGraph(result, level + 1);
//			} else {
//				Iterator modifiers = nav.modifiers();
//				while (modifiers.hasNext()) {
//					IModifier modifier = (IModifier) modifiers.next();
//					this.getPosition().addOption(modifier.getName(), modifier.getValue(), modifier.getCount());
//				}
//			}
//		}
	}

	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof NavigationGraph)) {
			return false;
		}
		NavigationGraph rhs = (NavigationGraph) object;
		
		return this.graphName.equals(rhs.graphName);
	}

	public String getGraphName() {
		return graphName;
	}

}
