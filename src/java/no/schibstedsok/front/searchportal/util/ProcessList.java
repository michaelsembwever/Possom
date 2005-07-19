/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A ProcessList.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class ProcessList {

	public List processList = new ArrayList();
	
	private static ProcessList instance;

	public static ProcessList getInstance(){
		if(instance == null)
			instance = new ProcessList();
		return instance;
	}
	
	/**
	 * Create a new ProcessList.
	 * 
	 */
	private ProcessList() {
	}

	public List getProcessList() {
		return processList;
	}
	
	
}
