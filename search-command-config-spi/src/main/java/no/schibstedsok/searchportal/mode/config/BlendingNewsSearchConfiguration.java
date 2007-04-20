// Copyright (2007) Schibsted Søk AS
/*
 * BlendingNewsSearchConfiguration.java
 *
 * Created on May 12, 2006, 2:20 PM
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.List;
import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("BlendingNewsSearchCommand")
public final class BlendingNewsSearchConfiguration extends FastSearchConfiguration {

    private List<String> filtersToBlend;
    private int documentsPerFilter;
    
    /**
     * 
     * @param asc 
     */
    public BlendingNewsSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    /**
     * 
     */
    public BlendingNewsSearchConfiguration() {
        super(null);
    }

    /**
     * 
     * @return 
     */
    public int getDocumentsPerFilter() {
        return documentsPerFilter;
    }

    /**
     * 
     * @return 
     */
    public List<String> getFiltersToBlend() {
        return filtersToBlend;
    }

    /**
     * 
     * @param documentsPerFilter 
     */
    public void setDocumentsPerFilter(final int documentsPerFilter) {
        this.documentsPerFilter = documentsPerFilter;
    }

    /**
     * 
     * @param filtersToBlend 
     */
    public void setFiltersToBlend(final List<String> filtersToBlend) {
        this.filtersToBlend = filtersToBlend;
    }

}
