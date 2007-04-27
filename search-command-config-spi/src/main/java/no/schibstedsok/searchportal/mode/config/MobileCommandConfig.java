// Copyright (2007) Schibsted Søk AS
/*
 * MobileCommandConfig.java
 *
 * Created on March 10, 2006, 2:25 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("MobileSearchCommand")
public class MobileCommandConfig extends CommandConfig {

    private String source;
    private String personalizationGroup;
    private String telenorPersonalizationGroup;
    private String sortBy;
    private String filter;

    /**
     * 
     * @param asc 
     */
    public MobileCommandConfig(final SearchConfiguration asc) {

        if(asc != null && asc instanceof MobileCommandConfig){
            final MobileCommandConfig cfg = (MobileCommandConfig) asc;
            source = cfg.source;
            personalizationGroup = cfg.personalizationGroup;
            telenorPersonalizationGroup = cfg.telenorPersonalizationGroup;
            sortBy = cfg.sortBy;
            filter = cfg.filter;
        }
    }

    /**
     * 
     * @return 
     */
    public String getSource() {
        return source;
    }
    
    /**
     * 
     * @param source 
     */
    public void setSource(final String source) {
        this.source = source;
    }

    /**
     * 
     * @return 
     */
    public String getPersonalizationGroup() {
        return personalizationGroup;
    }
    
    /**
     * 
     * @param group 
     */
    public void setPersonalizationGroup(final String group) {
        this.personalizationGroup = group;
    }

    /**
     * 
     * @return 
     */
    public String getTelenorPersonalizationGroup() {
        return telenorPersonalizationGroup;
    }

    /**
     * 
     * @param telenorPersonalizationGroup 
     */
    public void setTelenorPersonalizationGroup(final String telenorPersonalizationGroup) {
        this.telenorPersonalizationGroup = telenorPersonalizationGroup;
    }
    
    /**
     * 
     * @return 
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * 
     * @param sortBy 
     */
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * 
     * @param filter 
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * 
     * @return 
     */
    public String getFilter() {
        return filter;
    }
}
