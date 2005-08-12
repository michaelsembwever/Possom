
package no.schibstedsok.front.searchportal.filters.tv;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.analyzer.Analyzer;
import no.schibstedsok.front.searchportal.analyzer.AnalyzerEngine;
import no.schibstedsok.front.searchportal.configuration.BaseSearchConfiguration;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

/**
 * A TV-Filter.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class TvFilter extends AsynchronusBaseFilter {
    
	protected FilterConfig filterConfig = null;
        
    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- TV - Searching");
		
		SearchConfiguration configuration = new BaseSearchConfiguration(); 
		configuration.setTemplate(VelocityTemplates.TV_SEARCH);
		
		Analyzer analyzer = AnalyzerEngine.getAnalyzer();
		String query = request.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY);
		
		/** trim away the trigger words when doing the search in the TV-index */
		query = analyzer.replace(query);
		configuration.setQuery(query);
		
		/** start this search in separate thread */
        doSearch(response, configuration);
			
        /** continue in chain */
		chain.doFilter(request, response);

    }

    /** 
	 * 
	 * @param response
	 * @param configuration
	 * 
	 */
	private void doSearch(ServletResponse response, SearchConfiguration configuration) {
		final SearchConsumer w = new TvSearchConsumer(response,configuration);
        Thread searchThread = new Thread(w);
        searchThread.start();		
	}
	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;
	}
}
