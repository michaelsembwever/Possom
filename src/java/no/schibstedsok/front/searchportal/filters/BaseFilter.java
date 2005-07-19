package no.schibstedsok.front.searchportal.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.util.ProcessList;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;

/**
 * A BaseFilter.
 * 
 * This is an abstract filter concrete implementations
 * should use when constructing Schibsted søk filters.
 * 
 * Asynchrounous Filters should extend the AsynchronousBaseFilter
 * wich delegates the execute() method to doExecuteAsynch().
 * 
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public abstract class BaseFilter implements Filter {

	protected FilterConfig filterConfig = null;
	
	/** shared across instances per Class. */ 
	protected long maxTime = 0L;
	
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doExecute(request, response, chain);
	}

    public abstract void doExecute(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;
    
    public void destroy() {
        this.filterConfig = null;
    }

    public void init(FilterConfig filterConfig) {
        
		this.filterConfig = filterConfig;
		String filterName = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
		
		/** 
		 * Check if the filter should be part of standard pipeline
		 * that is allways axecuted.
		 */
		String partOfStdPipe = filterConfig.getInitParameter("standardPipeline");
		if(partOfStdPipe != null && (new Boolean(partOfStdPipe)).booleanValue()){
			ProcessList.getInstance().getProcessList().add(getClass().getName());
			filterConfig.getServletContext().log("Added " + getClass().getName() + " to standardPipeline");
		}
		
		String waitTime = filterConfig.getInitParameter("max-wait-time");
		if(waitTime != null) {
			maxTime = Long.parseLong(waitTime);
			filterConfig.getServletContext().log(filterName + " maxTime: " + maxTime);
		}
		
    }

	/**
	 * 
	 * Set up the SearchConfiguration parameters based on the Request.
	 * 
	 * @param request
	 * @param configuration
	 * @return
	 */
	public SearchConfiguration setUpSearchConfiguration(ServletRequest request, SearchConfiguration configuration) {

		int offset = 0;
		String query = "";
		String language = SearchConstants.DEFAULT_LANGUAGE;		//default
		int maxResults = SearchConstants.DEFAULT_DOCUMENTS_TO_RETURN;
		if (request.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY) != null)
	        query = request.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY);
		if (request.getParameter(SearchConstants.REQUEST_KEYPARAM_LANGUAGE) != null)
	        language  = request.getParameter(SearchConstants.REQUEST_KEYPARAM_LANGUAGE);
		if(request.getParameter(SearchConstants.REQUEST_KEYPARAM_DOCUMENTS_TO_RETURN) != null) {
			try {
				maxResults = Integer.parseInt(request.getParameter(SearchConstants.REQUEST_KEYPARAM_DOCUMENTS_TO_RETURN));				
			} catch (Exception e) {
				filterConfig.getServletContext().log("Wrong format for number of documents to return. Using default " + SearchConstants.DEFAULT_DOCUMENTS_TO_RETURN);
			}
		}
		if(request.getParameter(SearchConstants.REQUEST_KEYPARAM_OFFSET) != null) {
			try {
				offset = Integer.parseInt(request.getParameter(SearchConstants.REQUEST_KEYPARAM_OFFSET));				
			} catch (Exception e) {
				filterConfig.getServletContext().log("Wrong format for offset. Using default 0");
			}
		}

		if (configuration instanceof FastSearchConfiguration) {
			FastSearchConfiguration config = (FastSearchConfiguration) configuration;
			config.setDocsToReturn(maxResults);
			config.setMaxTime(100000);
			config.setOffSet(offset);
			config.setQuery(query);
			return config;
		}
		
		return configuration;
	}

}
