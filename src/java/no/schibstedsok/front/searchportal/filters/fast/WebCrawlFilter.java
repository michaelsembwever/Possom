
package no.schibstedsok.front.searchportal.filters.fast;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

/**
 * A FastFilter.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class WebCrawlFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;
    protected boolean isSynchronous = true;
    
//    public static final String DEFAULT_PROPS = "/velocity.properties";
    
    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- Fast WebCrawl - Searching");
			
		FastSearchConfiguration configuration = new FastSearchConfigurationImpl(); 
		configuration = (FastSearchConfiguration) setUpSearchConfiguration(request, configuration);
		configuration.setCollection(SearchConstants.WEBCRAWL_COLLECTION);
		configuration.setTemplate(VelocityTemplates.WEBCRAWL_COLLECTION_SEARCH);
		
		// start this search in separate thread
        doSearch(response, configuration, request);

		// go to next filter in chain
        chain.doFilter(request, response);

    }

    /** 
	 * 
	 * @param response
     * @param configuration
     * @param request
     */
	private void doSearch(ServletResponse response, SearchConfiguration configuration, ServletRequest request) {
		final SearchConsumer w = new FastSearchConsumer(response,configuration);
        startThread(w, request);
	}
	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;
	}
}
