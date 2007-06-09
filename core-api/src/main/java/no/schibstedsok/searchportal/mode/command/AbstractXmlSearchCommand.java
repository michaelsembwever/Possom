/*
 * Copyright (2006-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;

import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.mode.config.AbstractXmlSearchConfiguration;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

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

    private final HTTPClient client;

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

        client = HTTPClient.instance(host, port, conf.getHostHeader());
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
