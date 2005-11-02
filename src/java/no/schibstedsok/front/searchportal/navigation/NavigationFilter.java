package no.schibstedsok.front.searchportal.navigation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A NavigationFilter.
 * 
 * A helper that handles injection and outjection of a NavigationHandler
 * for a unique User.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class NavigationFilter implements Filter {

	private static final String FILTER_APPLIED = "filterapplied";
	protected FilterConfig filterConfig = null;
	private static Log log = LogFactory.getLog(NavigationFilter.class);
	
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		//NB! remember to handle (late) exceptions in filter.
		HttpServletRequest rq = (HttpServletRequest) request;
		
         
		try {
			 
			//get info about what the user is requesting
			String collection = rq.getParameter(SearchConstants.REQUEST_KEYPARAM_COLLECTION) != null ? rq.getParameter(SearchConstants.REQUEST_KEYPARAM_COLLECTION) : SearchConstants.REQUEST_PARAM_DEFAULT_COLLECTIONS;
			String query = rq.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY) != null ? rq.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY) : ""; 
			
			String upSelected = rq.getParameter("up") != null ? rq.getParameter("up") : null;
			String selected = rq.getParameter("select") != null ? rq.getParameter("select") : null;
			String selectedGraph = rq.getParameter("nav") != null ? rq.getParameter("nav") : "";
	        
	        //old navigation 
			if (rq.getParameter("nn") == null) {

				//reset new navigation 
				NavigationContext.set(null);
				rq.getSession().setAttribute("navigationHandler", null);
	            chain.doFilter(rq, response);
	            
	        } else {
							
	        	//get a NavigationHandler
		    	NavigationHandler handler = getHandler(rq.getSession(), collection, query, selectedGraph);
					  
				navigate(upSelected, selected, selectedGraph, handler);
						
				//inject the NavigationHandler to the running thread.
				NavigationContext.set(handler);
				
				chain.doFilter(rq, response);
				
				//outject the updated navigationHandler to the user session.
				rq.getSession().setAttribute("navigationHandler", NavigationContext.get());
				
			}
			
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		} finally {
							
//			if(errorOccurred)
//				System.out.println("A late error occured!");
		}
	}

	private void navigate(String upSelected, String selected, String selectedGraph, NavigationHandler handler) {
		if(!"".equals(selectedGraph)) {
			
			NavigationGraph graph = handler.getGraph(selectedGraph);
			log.debug("FilterDebug " + graph.getPosition().createFilter());
			
			//up or down navigation?
			if(upSelected != null)
				graph.up(selected);
			else {
				graph.select(selected);
			}
		}
	}

	private NavigationHandler getHandler(HttpSession session, String collection, String query, String graph) {
		
		NavigationHandler handler = session.getAttribute("navigationHandler")!= null ? (NavigationHandler)session.getAttribute("navigationHandler") : null;
		String prevGraphSelected = (String)session.getAttribute("previousSelectedGraph");
		
		//if user changes tab, graph, query or no previous Navigation handler (first request)
		if(handler == null || (prevGraphSelected == null || !prevGraphSelected.equals(graph)) || (handler != null &! (handler.getCollection().equals(collection) && handler.getQuery().equals(query)))) {
			session.setAttribute("previousSelectedGraph", graph);
			handler = NavigationHandler.getNewHandler(collection, query);
		}
		
		return handler;
	}

    public void destroy() {
        this.filterConfig = null;
    }

    public void init(FilterConfig filterConfig) {
    	this.filterConfig = filterConfig;
    }
    	
	/**
	 * Print info.
	 * @param geographicGraph
	 */
	private void printFilterInfo(NavigationGraph geographicGraph) {
		System.out.println("------------------> NavigationFilter " + geographicGraph.getPosition().createFilter());
	}
	

	/**
	 * Print info.
	 * @param elements
	 */
	private void printElementsInfo(List elements) {
		Iterator iter = elements.iterator();
		while (iter.hasNext()) {
			NavigationElement element = (NavigationElement) iter.next();
			System.out.println("------------------> " + element);
		}
	}
	
	/**
	 * Print info.
	 * @param navigators
	 */
	private void printNavigatorInfo(List navigators) {
		
		Iterator iter = navigators.iterator();
		while (iter.hasNext()) {
			FastNavigator navigator = (FastNavigator)iter.next();
			log.debug(navigator.getName());
			log.debug(navigator.getField());
			log.debug(navigator.getDisplayName());
			
		}
	}
	
	/**
	 * Does debug level logging. check for debugLevel before calling this method.
	 * 
	 * Print info.
	 * @param collection
	 */
	private void printGraphInfo(NavigationHandler handler) {
		
		Iterator iter = handler.getGraphs().iterator();
//		System.out.println("------------------> NAVIGATION DEBUG HandlerId=:" + handler.getHandlerId());
		while (iter.hasNext()) {
			NavigationGraph graph = (NavigationGraph)iter.next();
//			System.out.println("------------------>" + graph);
			printFilterInfo(graph);
			System.out.println("------------------> NAVIGATION DEBUG OPTIONS FOUND :" + graph.getPosition().options().size() );
//			printElementsInfo(graph.getPosition().options());
		}
	}


}
