// Copyright (2007) Schibsted Søk AS
/*
 * MobileCommandConfig.java
 *
 * Created on March 10, 2006, 2:25 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("MobileSearchCommand")
public final class MobileCommandConfig extends CommandConfig {

    private String source;
    private String personalizationGroup;
    private String telenorPersonalizationGroup;
    private String sortBy;
    private String filter;

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

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {

        super.readSearchConfiguration(element, inherit);

        // TODO use fillBeanProperty pattern instead

        if(inherit != null && inherit instanceof MobileCommandConfig){
            final MobileCommandConfig cfg = (MobileCommandConfig) inherit;
            source = cfg.source;
            personalizationGroup = cfg.personalizationGroup;
            telenorPersonalizationGroup = cfg.telenorPersonalizationGroup;
            sortBy = cfg.sortBy;
            filter = cfg.filter;
        }
        setPersonalizationGroup(element.getAttribute("personalization-group"));
        setTelenorPersonalizationGroup(element.getAttribute("telenor-personalization-group"));
        setSortBy(element.getAttribute("sort-by"));
        setSource(element.getAttribute("source"));
        setFilter(element.getAttribute("filter"));

        return this;
    }


}
