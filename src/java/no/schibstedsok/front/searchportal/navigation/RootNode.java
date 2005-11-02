package no.schibstedsok.front.searchportal.navigation;

import java.io.Serializable;

/**
 * @author Lars Johansson
 *
 */
public class RootNode extends NavigationNode implements Serializable {
	
	public RootNode() {
		super();
		super.levelName = "Top";
		super.level = 0;
		super.filter = "";		//default no filter at root level
	}
	
	public String createFilter() {
		return super.filter;
	}

}
