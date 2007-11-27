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

import no.sesat.search.http.HTTPClient;
import no.sesat.search.mode.config.AbstractXmlSearchConfiguration;
import no.sesat.search.site.config.SiteConfiguration;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractXmlSearchCommand extends AbstractSearchCommand {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractXmlSearchCommand.class);

    // Attributes ----------------------------------------------------

    private final transient HTTPClient client;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new xml based command.
     *
     * @param cxt The context to execute in.
     */
    public AbstractXmlSearchCommand(final Context cxt) {
        super(cxt);

        final AbstractXmlSearchConfiguration conf = (AbstractXmlSearchConfiguration)cxt.getSearchConfiguration();

        final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();
        final String host = siteConf.getProperty(conf.getHost());
        final int port = Integer.parseInt(siteConf.getProperty(conf.getPort()));

        client = HTTPClient.instance(conf.getHostHeader().length() > 0 ? conf.getHostHeader() : host, port, host);
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /**
     * 
     * @return 
     */
    protected abstract String createRequestURL();

    /**
     * 
     * @return 
     */
    protected int getResultsToReturn(){
        return context.getSearchConfiguration().getResultsToReturn();
    }

    /**
     * 
     * @return 
     * @throws java.io.IOException 
     * @throws org.xml.sax.SAXException 
     */
    protected final Document getXmlResult() throws IOException, SAXException {
        final String url = createRequestURL();
        DUMP.info("Using " + url);
        return client.getXmlDocument(url);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
