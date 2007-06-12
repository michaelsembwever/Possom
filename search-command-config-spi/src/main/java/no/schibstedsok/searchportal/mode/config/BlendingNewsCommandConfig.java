// Copyright (2007) Schibsted Søk AS
/*
 * BlendingNewsCommandConfig.java
 *
 * Created on May 12, 2006, 2:20 PM
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("BlendingNewsSearchCommand")
public final class BlendingNewsCommandConfig extends  NewsCommandConfig {

    private List<String> filtersToBlend;
    private int documentsPerFilter;
    
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

    @Override
    public FastCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        // TODO use fillBeanProperty pattern instead
        final String[] filters = element.getAttribute("filters").split(",");

        final List<String> filterList = new ArrayList<String>();

        for (String filter : filters) {
            filterList.add(filter.trim());
        }
        
        setFiltersToBlend(filterList);
        setDocumentsPerFilter(Integer.parseInt(element.getAttribute("documentsPerFilter")));

        return this;
    }
    
    

}
