package no.schibstedsok.front.searchportal.navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * NavigationNode. 
 * 
 * A point in the graph holding information regarding 
 * "options", filter String for the Node and a pointer 
 * to the parent.
 * 
 * 
 * @author Lars Johansson
 *
 */
public class NavigationNode implements Serializable {
	
	/** the level/depth for this node */
	int level;
	/** Filter is a complete level and option representation to send to Fast. */
	String filter;
	String levelName;
	
	NavigationNode parent = null;
	private List options = new ArrayList();
	private boolean visible = true;
	
	
	public NavigationNode() {
		
	}

	/**
	 * Creates a Fast filter based on the NavigationNode.
	 * 
	 * @param buf
	 * @return
	 */
	protected String createFilter(StringBuffer buf) {
		
		if(!filter.equals("")){
			buf.append(" +").append(filter).append("");
		}
		
		if(parent != null)
			parent.createFilter(buf);
		
		String result = buf.toString();
		return result;
	}

	/**
	 * @return The Node filter on.
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Add an selectable option to the Node 
	 *  
	 * @param name
	 * @param value
	 * @param count
	 */
	public void addOption(String name, String value, int count) {
		
		NavigationElement element = new NavigationElement(name, value, count);
		
		if(!this.options.contains(element)) {
			this.options.add(element);
		}
			
	}

	public List options() {
		return options;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		
	}

	/**
	 * See createFilter(StringBuffer buffer)
	 * 
	 * @return
	 */
	public String createFilter() {
		StringBuffer buffer = new StringBuffer();
		return createFilter(buffer);
	}


	public NavigationNode getParent() {
		return parent;
	}

	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(levelName).append(": ");
			
		Iterator options = options().iterator();
		while (options.hasNext()) {
			NavigationElement option = (NavigationElement) options.next();
			buf.append(option.getName()).append(" ");
			
		}
		return buf.toString();
	}
	
	
	/**
	 * Get the complete upwards hierarchy for this node
	 * @return
	 */
	public List getParents() {
		List parents = new ArrayList();
		NavigationNode parent = getParent();
		while(parent != null) {
			if(parent.isVisible())
				parents.add(parent);
			parent = parent.getParent();
		}
		return parents;
	}

	/**
	 * The name of this node
	 * @return
	 */
	public String getName() {
		return levelName;
	}

	/**
	 * The name of this node
	 * @param levelName
	 */
	public void setName(String levelName) {
		this.levelName = levelName;
	}

	/** 
	 * If the node is to be visible (you might wish to hide emtpy nodes for instance...)
	 * @param b
	 */
	public void setVisible(boolean b) {
		this.visible  = b;
	}

	public boolean isVisible() {
		return visible;
	}
	
}
