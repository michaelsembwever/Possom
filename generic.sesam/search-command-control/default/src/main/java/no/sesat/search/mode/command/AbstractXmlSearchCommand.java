/*
 * Copyright (2006-2008) Schibsted Søk AS
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
package no.sesat.search.mode.command;


import no.sesat.search.result.ResultItem;

import org.w3c.dom.Element;

/**
 * Helper base implementation for search commands that are RESTful and have XML responses.
 *
 * The RESTful server is defined through:
 * host: AbstractXmlSearchConfiguration.getHost()
 * port: AbstractXmlSearchConfiguration.getPort()
 *
 * @version $Id$
 */
public abstract class AbstractXmlSearchCommand extends AbstractSearchCommand{


    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractXmlSearchCommand.class);

    // Attributes ----------------------------------------------------

    private XmlRestful restful;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new xml based command.
     *
     * @param cxt The context to execute in.
     */
    protected AbstractXmlSearchCommand(final Context cxt) {
        super(cxt);
    }

    // Public --------------------------------------------------------

    public String createRequestURL() {

        return restful.createRequestURL();
    }



    // Protected -----------------------------------------------------


    /** Each individual result is usually defined within one given Element.
     *
     * @param result the w3c element
     * @return the ResultItem
     */
    protected abstract ResultItem createItem(final Element result);

    protected final XmlRestful getXmlRestful(){
        return restful;
    }

    protected final void setXmlRestful(final XmlRestful restful){
        this.restful = restful;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
