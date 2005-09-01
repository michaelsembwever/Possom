/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal.filters.picsearch;

import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.configuration.BaseSearchConfiguration;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PicSearchFilter extends AsynchronusBaseFilter {
    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SearchConfiguration configuration = new BaseSearchConfiguration();
        setUpSearchConfiguration(request, configuration);
        configuration.setQuery(request.getParameter("q"));
        if (request.getParameter("offset") !=null)
            configuration.setOffSet(Integer.parseInt(request.getParameter("offset")));
        configuration.setTemplate(VelocityTemplates.PIC_SEARCH);
        doSearch(response, configuration, request);
    }

    /**
     *
     * @param response
     * @param configuration
     */
    private void doSearch(ServletResponse response, SearchConfiguration configuration, ServletRequest request) {
        SearchConsumer w = null;

        try {
            w = new PicSearchConsumer(response.getWriter(), configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startThread(w, request);
    }


    public void init(FilterConfig filterConfig) {
        super.init(filterConfig);
        this.filterConfig = filterConfig;
    }
}
