package no.schibstedsok.front.searchportal.navigation;

import java.util.HashMap;

/**
 * 
 * @deprecated 
 * Until we see the need for a persistent Storage of 
 * objects on the server-side, don't use this. When you decide to
 * do, re-implement this so that id's are globally unique!
 * 
 * @author Lars Johansson
 *
 */
public class NavigationHandlerStore {

	//TODO: implement a timeout mechanism.
	
	private static HashMap handlers;
	private static NavigationHandlerStore instance;

	private NavigationHandlerStore() {	//hide
		handlers = new HashMap();
	}
	
	public synchronized static NavigationHandlerStore getInstance() {
		if(instance == null) 
			instance = new NavigationHandlerStore();
		return instance;
	}

	/**
	 * @deprecated REMOVED Until we assign id 
	 * @param handler
	 * @return
	 */
	public static NavigationHandler add(NavigationHandler handler) {
		return null;
//		return (NavigationHandler)handlers.put(handler.getHandlerId(), handler);
			
	}

	public static NavigationHandler lookUp(String id) {
		if(handlers.containsKey(id)) {
			return (NavigationHandler)handlers.get(id);
		} else {
			return null;
		}
	}
}
