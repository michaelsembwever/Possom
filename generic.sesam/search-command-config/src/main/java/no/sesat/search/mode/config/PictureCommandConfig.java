/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.mode.config;

import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.mode.config.querybuilder.InfixQueryBuilderConfig;

/** A search command that uses the picsearch API.
 * {@link http://www.picsearch.com/}
 * {@link http://about.picsearch.com/image_search/}
 *
 * @version <tt>$Id$</tt>
 */
@Controller("PicSearchCommand")
public class PictureCommandConfig extends AbstractXmlSearchConfiguration {

    //private static final Logger LOG = Logger.getLogger(PictureCommandConfig.class);

    private String filter = "medium";
    private String customerId = "558735";
    private String site = "";
    private String domainBoost = "";

    public PictureCommandConfig(){

        super();

        ((InfixQueryBuilderConfig)getQueryBuilder()).setNotPrefix("-");

    }

    /**
     * Returns the offensive content filtering level.
     *
     * @return Filtering level.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Returns the customer id to use for picsearch queries associated with this configuration.
     *
     * @return The customer id.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets property filter.
     *
     * @param filter New value for filter.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Sets property customerId
     *
     * @param customerId New value for customerId
     */
    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the site to which searches should be restricted.
     *
     * @return the site to which searches are restricted.
     */
    public String getSite() {
        return site;
    }

    /**
     * Set this to restrict the searches to <tt>site</tt>
     *
     * @param site to restrict searches to (e.g. dn.se).
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Returns the domain boost to use.
     *
     * @return the domain boost.
     */
    public String getDomainBoost() {
        return domainBoost;
    }

    /**
     * Sets the domain boost. Example of domain boost string: se=100,nu=100,dk=50.
     *
     * @param domainBoost the domain boost string.
     */
    public void setDomainBoost(String domainBoost) {
        this.domainBoost = domainBoost;
    }
}
