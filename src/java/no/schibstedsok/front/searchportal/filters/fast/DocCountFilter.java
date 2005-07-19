
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
 * A FastFilter for getting the number of documents in all collections..
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class DocCountFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;

    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- Fast DocCounter - Searching");
		
		FastSearchConfiguration configuration = new FastSearchConfigurationImpl(); 
		configuration = (FastSearchConfiguration) setUpSearchConfiguration(request, configuration);

		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
		configuration.setNavigatorString(SearchConstants.COUNTERNAVIGATORSTRING);
		configuration.setTemplate(VelocityTemplates.LOCAL_COUNT);

		configuration.setDocsToReturn(1);

		// start this search in separate thread
        doSearch(response, configuration);

		// go to next filter in chain
        chain.doFilter(request, response);

    }

    /**
     * 
     * Does the actual search in a separate thread.
     * 
     * @param query
     * @param response
     * @param configuration 
     * @param targetCollection 
     *
     */
    private void doSearch(ServletResponse response, SearchConfiguration configuration) {
        
        final SearchConsumer w = new FastSearchConsumer(response, configuration);
        Thread searchThread = new Thread(w);
        searchThread.start();
        
    }
	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;
	}
}
