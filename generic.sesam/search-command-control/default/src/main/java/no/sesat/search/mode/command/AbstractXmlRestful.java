/*
 * Copyright (2006-2007) Schibsted Søk AS
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
public abstract class AbstractXmlRestful implements XmlRestful{


    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractXmlSearchCommand.class);
    private static final Logger DUMP = Logger.getLogger("no.sesat.search.Dump");

    // Attributes ----------------------------------------------------

    private final Context context;

    private final transient HTTPClient client;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new xml based command.
     *
     * @param cxt The context to execute in.
     */
    public AbstractXmlRestful(final Context cxt) {

        context = cxt;

        final AbstractXmlSearchConfiguration conf = (AbstractXmlSearchConfiguration)cxt.getSearchConfiguration();

        final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();
        final String host = siteConf.getProperty(conf.getHost());
        final int port = null != siteConf.getProperty(conf.getPort())
                ? Integer.parseInt(siteConf.getProperty(conf.getPort()))
                : 80; // defaults to normal http port

        client = HTTPClient.instance(conf.getHostHeader().length() > 0 ? conf.getHostHeader() : host, port, host);
    }

    // Public --------------------------------------------------------

    public final Document getXmlResult() throws IOException, SAXException {
        final String url = createRequestURL();
        DUMP.info("Using " + url);
        return client.getXmlDocument(url);
    }

    public final BufferedReader getHttpReader(final String encoding) throws IOException {
        final String url = createRequestURL();
        DUMP.info("Using " + url);
        return client.getBufferedReader(url, encoding);
    }

    // Protected -----------------------------------------------------

    public Context getContext(){
        return context;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
