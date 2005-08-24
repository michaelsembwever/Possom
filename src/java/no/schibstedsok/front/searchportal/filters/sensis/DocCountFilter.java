
package no.schibstedsok.front.searchportal.filters.sensis;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisFastSearchConfiguration;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

/**
 * A Sensis FastFilter for getting the number of documents in all collections..
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class DocCountFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;

    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- Sensis DocCounter - Searching");
		
		FastSearchConfiguration configuration = new SensisFastSearchConfiguration(); 
		configuration = (FastSearchConfiguration)setUpSearchConfiguration(request, configuration);

		configuration.setLanguage(SearchConstants.LANGUAGE_ENGLISH);
		configuration.setDocsToReturn(1);
		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
		configuration.setTemplate(VelocityTemplates.GLOBAL_COUNT);
		
		// start this search in separate thread
        doSearch(response, configuration, request);

        chain.doFilter(request, response);

    }


    /**
     *
     * Does the actual search in a separate thread.
     *
     * @param response
     * @param configuration
     * @param request
     *
     */
    private void doSearch(ServletResponse response, SearchConfiguration configuration, ServletRequest request) {

        final SearchConsumer w = new SensisSearchConsumer(response, configuration);
        startThread(w, request);        
    }

	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;
	}
}
