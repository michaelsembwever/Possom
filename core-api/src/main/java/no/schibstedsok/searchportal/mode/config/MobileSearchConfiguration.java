// Copyright (2007) Schibsted Søk AS
/*
 * MobileSearchConfiguration.java
 *
 * Created on March 10, 2006, 2:25 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.mode.command.MobileSearchCommand;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.run.RunningQuery;

/**
 *
 * @author magnuse
 */
public class MobileSearchConfiguration extends AbstractSearchConfiguration {

    private String source;
    private String personalizationGroup;
    private String telenorPersonalizationGroup;
    private String sortBy;
    private String filter;

    public MobileSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
        if(asc != null && asc instanceof MobileSearchConfiguration){
            final MobileSearchConfiguration cfg = (MobileSearchConfiguration) asc;
            source = cfg.source;
            personalizationGroup = cfg.personalizationGroup;
            telenorPersonalizationGroup = cfg.telenorPersonalizationGroup;
            sortBy = cfg.sortBy;
            filter = cfg.filter;
        }
    }

    public String getSource() {
        return source;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }

    public String getPersonalizationGroup() {
        return personalizationGroup;
    }
    
    public void setPersonalizationGroup(final String group) {
        this.personalizationGroup = group;
    }

    public String getTelenorPersonalizationGroup() {
        return telenorPersonalizationGroup;
    }

    public void setTelenorPersonalizationGroup(final String telenorPersonalizationGroup) {
        this.telenorPersonalizationGroup = telenorPersonalizationGroup;
    }
    
    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }
}
