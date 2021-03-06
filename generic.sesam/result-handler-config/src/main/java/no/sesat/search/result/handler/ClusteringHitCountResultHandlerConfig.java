/* Copyright (2012) Schibsted ASA
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
package no.sesat.search.result.handler;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;

/**
 * Result handler to fix the hit count on clustering search results where Fast gives us a wrong hit count (too low).
 * Uses the navigator counts instead since these are correct.
 *
 * @version $Id$
 */
@Controller("ClusteringHitCountResultHandler")
public class ClusteringHitCountResultHandlerConfig extends AbstractResultHandlerConfig {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ClusteringHitCountResultHandlerConfig.class);

    private static final long serialVersionUID = 7021379415579338168L;

    private static final String DEFAULT_NAV_ID = "year";

    // Attributes ----------------------------------------------------

    private String navId;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        super.readResultHandler(element);

        LOG.debug("nav-id: '" + element.getAttribute("nav-id") + "'");
        setNavId(element.getAttribute("nav-id") != null && element.getAttribute("nav-id") != ""
            ? element.getAttribute("nav-id") : DEFAULT_NAV_ID);

        return this;
    }

    // Getters / Setters ---------------------------------------------

    /** The Nav Id to search in to find the correct hit count.
     * @return the navId
     */
    public String getNavId() {
        return navId;
    }

    /** @see #getNavId()
     * @param navId the navId to set
     */
    public void setNavId(final String navId) {
        this.navId = navId;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
