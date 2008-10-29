/* Copyright (2006-2008) Schibsted Søk AS
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
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Id$
 */
public abstract class AbstractXmlSearchConfiguration extends CommandConfig {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    /**
     * Holds value of property key for host.
     */
    private String host = "";

    /**
     * Holds value of property encoding.
     */
    private String encoding = "";

    /**
     * Holds value of property key for port.
     */
    private String port = "";

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(final String host) {
        this.host = host;
    }



    /**
     * Getter for property encoding.
     * @return Value of property encoding.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Setter for property encoding.
     * @param encoding New value of property encoding.
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }



    /**
     * Getter for property port.
     * @return Value of property port.
     */
    public String getPort() {
        return this.port;
    }

    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(final String port) {
        this.port = port;
    }

    /**
     * Holds value of property hostHeader.
     */
    private String hostHeader = "";

    /**
     * Getter for property hostHeader.
     * @return Value of property hostHeader.
     */
    public String getHostHeader() {
        return this.hostHeader;
    }

    /**
     * Setter for property hostHeader.
     * @param hostHeader New value of property hostHeader.
     */
    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
