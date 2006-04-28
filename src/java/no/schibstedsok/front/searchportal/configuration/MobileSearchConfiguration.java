/*
 * MobileSearchConfiguration.java
 *
 * Created on March 10, 2006, 2:25 PM
 *
 */

package no.schibstedsok.front.searchportal.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.command.MobileSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;

/**
 *
 * @author magnuse
 */
public class MobileSearchConfiguration extends AbstractSearchConfiguration {

    private String source;
    private String personalizationGroup;
    private String sortBy;

    public MobileSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
        if(asc != null && asc instanceof MobileSearchConfiguration){
            final MobileSearchConfiguration cfg = (MobileSearchConfiguration) asc;
            source = cfg.source;
            personalizationGroup = cfg.personalizationGroup;
            sortBy = cfg.sortBy;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }
}
