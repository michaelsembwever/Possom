/* Copyright (2008) Schibsted Søk AS
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
 * AbstractXmlSearchConfiguration.java
 *
 * Created on June 12, 2006, 10:58 AM
 *
 */

package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Searching against a Solr index using the Solrj client.
 *
 * @version $Id$
 */
@Controller("SolrSearchCommand")
public class SolrCommandConfig extends CommandConfig {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    /**
     * Holds value of property key for serverUrl.
     */
    private String serverUrl = "";

    private String querySuffix = "";

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /**
     * Getter for property serverUrl.
     * The value returned is the key used
     *   to look up the real value via SiteConfiguration(via in configuration.properties)
     *
     * @return Value of property serverUrl.
     */
    public String getServerUrl() {
        return this.serverUrl;
    }

    /**
     * Setter for property serverUrl.
     * @param serverUrl New value of property serverUrl.
     */
    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getQuerySuffix(){
        return querySuffix;
    }

    /** A string to append to every query. Used as an additional filter.
     *
     * @param querySuffix
     */
    public void setQuerySuffix(final String querySuffix){
        this.querySuffix = querySuffix;
    }

    @Override
    public SolrCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {

        super.readSearchConfiguration(element, inherit, context);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "serverUrl", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "querySuffix", ParseType.String, element, "");

        return this;
    }



    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
