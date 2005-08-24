
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
public final class WikiFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;
    
    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- FastWiki - Searching");

		FastSearchConfiguration configuration = new FastSearchConfigurationImpl();
		configuration = (FastSearchConfiguration) setUpSearchConfiguration(request, configuration);
		configuration.setCollection(SearchConstants.WIKI_COLLECTION);
		configuration.setTemplate(VelocityTemplates.WIKI_COLLECTION_SEARCH);
		configuration.setDocsToReturn(1);
        
        //Do a wikititle exact match
        StringBuffer buf = new StringBuffer("wikititle:");
        buf.append(configuration.getQuery());
//        buf.append("$");
        configuration.setQuery(buf.toString());

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
