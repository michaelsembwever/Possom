// Copyright (2007) Schibsted Søk AS
/*
 * BlendingNewsSearchConfiguration.java
 *
 * Created on May 12, 2006, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.List;

/**
 *
 * @author maek
 */
public class BlendingNewsSearchConfiguration extends FastSearchConfiguration {

    private List<String> filtersToBlend;
    private int documentsPerFilter;
    
    public BlendingNewsSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    public BlendingNewsSearchConfiguration() {
        super(null);
    }

    public int getDocumentsPerFilter() {
        return documentsPerFilter;
    }

    public List<String> getFiltersToBlend() {
        return filtersToBlend;
    }

    public void setDocumentsPerFilter(int documentsPerFilter) {
        this.documentsPerFilter = documentsPerFilter;
    }

    public void setFiltersToBlend(List<String> filtersToBlend) {
        this.filtersToBlend = filtersToBlend;
    }

}
