/*
 * Copyright (2006-2012) Schibsted ASA
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
package no.sesat.search.mode.command;

import java.io.IOException;
import java.io.BufferedReader;

import no.sesat.search.http.HTTPClient;
import no.sesat.search.mode.command.SearchCommand.Context;
import no.sesat.search.mode.config.AbstractXmlSearchConfiguration;
import no.sesat.search.site.config.SiteConfiguration;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Base implementation for search commands that are RESTful and have XML responses.
 *
 * The RESTful server is defined through:
 * host: AbstractXmlSearchConfiguration.getHost()
 * port: AbstractXmlSearchConfiguration.getPort()
 *
 * @version $Id$
 */
public abstract class AbstractXmlRestful extends AbstractRestful implements XmlRestful{


    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractXmlSearchCommand.class);
    private static final Logger DUMP = Logger.getLogger("no.sesat.search.Dump");

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new XmlRestful
     *
     * @param cxt The context to execute in.
     */
    public AbstractXmlRestful(final Context cxt) {

        super(cxt);
    }

    // Public --------------------------------------------------------

    @Override
    public final Document getXmlResult() throws IOException, SAXException {

        final String url = createRequestURL();
        DUMP.info("Using " + url);
        return getClient().getXmlDocument(url);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
