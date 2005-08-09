/*
 * Copyright (2005) Schibsted S¿k AS
 * 
 */
package no.schibstedsok.front.searchportal.filters.fast;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 *
 * A fast filter for company searches.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class CompaniesFilter extends AsynchronusBaseFilter {
    protected FilterConfig filterConfig = null;

    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- Companies - Searching");

        FastSearchConfiguration configuration = new FastSearchConfigurationImpl();
        configuration = (FastSearchConfiguration) setUpSearchConfiguration(request, configuration);
        configuration.setCollection(SearchConstants.COMPANIES_COLLECTION);
        configuration.setTemplate(VelocityTemplates.COMPANIES_COLLECTION_SEARCH);

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
