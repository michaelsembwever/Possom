
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

//	private SearchConfiguration setUpSearchConfiguration(ServletRequest request) {
//
//		int offset = 0;
//		String query = "";
//		String language = "no";		//default
//		int maxResults = 1;			//for navigators, ask for one document only.
//
//		if (request.getParameter("q") != null)
//	        query = request.getParameter("q");
//		if (request.getParameter("lan") != null)
//	        language  = request.getParameter("lan");
//		if(request.getParameter("o") != null) {
//			try {
//				offset = Integer.parseInt(request.getParameter("docs"));
//			} catch (Exception e) {
//				filterConfig.getServletContext().log("Wrong format for offset. Using default 0");
//			}
//		}
//		SearchConfiguration configuration = new SensisFastSearchConfiguration();
//		configuration.setCollection(SearchConstants.COUNTERNAVIGATOR);
//		configuration.setTemplate(VelocityTemplates.GLOBAL_COUNT);
//		configuration.setNavigatorString(SearchConstants.COUNTERNAVIGATORSTRING);
//		configuration.setLanguage(language);
//		configuration.setDocsToReturn(maxResults);
//		configuration.setMaxTime(100000);
//		configuration.setQuery(query);
//
//		return configuration;
//	}

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
