/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * MobileCommandConfig.java
 *
 * Created on March 10, 2006, 2:25 PM
 *
 */

package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;

/**
 *
 *
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
            final SearchConfiguration inherit,
            final Context context) {

        super.readSearchConfiguration(element, inherit, context);

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
