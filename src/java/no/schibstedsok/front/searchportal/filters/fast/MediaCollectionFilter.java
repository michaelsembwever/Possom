
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
 * A MediaFastFilter.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class MediaCollectionFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;
    
    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		filterConfig.getServletContext().log("- FastMedia - Searching");

		FastSearchConfiguration configuration = new FastSearchConfigurationImpl();
		configuration = (FastSearchConfiguration) setUpSearchConfiguration(request, configuration);
		configuration.setCollection(SearchConstants.MEDIA_COLLECTION);
		configuration.setTemplate(VelocityTemplates.MEDIA_COLLECTION_SEARCH);
			
		// start this search in separate thread
        doSearch(response, configuration);			

		// go to next filter in chain
        chain.doFilter(request, response);

    }

    /** 
	 * 
	 * @param response
	 * @param configuration
	 */
	private void doSearch(ServletResponse response, SearchConfiguration configuration) {
		final SearchConsumer w = new FastSearchConsumer(response,configuration);
        Thread searchThread = new Thread(w);
        searchThread.start();		
	}

	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;
	}

}
