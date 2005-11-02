package no.schibstedsok.front.searchportal.navigation;

public class NavigationContext {

	private static ThreadLocal threadLocal = new ThreadLocal();

    /**
     * Get NavigationHandler.
     * 
     * @param collection
     * @return The NavigationHandler.
     */
    public static NavigationHandler get() {
        if(threadLocal != null) {
        	NavigationHandler handler = (NavigationHandler) threadLocal.get();
            return handler;
        } else
        	return null;
    }

	/**
	 * Set the NavigationHandler on the threadLocal
	 * @param handler
	 */
	public static void set(NavigationHandler handler) {
		threadLocal.set(handler);
//		System.out.println("--------------> Assigned NavigationHandler to thread " + handler.getHandlerId());
	}

}
