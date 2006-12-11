// Copyright (2006) Schibsted Søk AS
/*
 * AbstractYahooSearchCommand.java
 *
 * Created on June 12, 2006, 10:51 AM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.util.Map;
import no.schibstedsok.searchportal.mode.config.AbstractYahooSearchConfiguration;
import no.schibstedsok.searchportal.http.HTTPClient;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractYahooSearchCommand extends AbstractSearchCommand {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractYahooSearchCommand.class);

    // Attributes ----------------------------------------------------

    private HTTPClient client;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new overture command.
     *
     * @param cxt The context to execute in.
     * @param parameters Search command parameters.
     */
    public AbstractYahooSearchCommand(
            final Context cxt,
            final Map<String, Object> parameters) {

        super(cxt, parameters);

        final AbstractYahooSearchConfiguration conf = (AbstractYahooSearchConfiguration)cxt.getSearchConfiguration();
        
        client = null != conf.getHostHeader() && conf.getHostHeader().length() >0
                ? HTTPClient.instance(conf.getName(), conf.getHost(), conf.getPort(), conf.getHostHeader())
                : HTTPClient.instance(conf.getName(), conf.getHost(), conf.getPort());
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected abstract String createRequestURL();

    protected int getResultsToReturn(){

        return context.getSearchConfiguration().getResultsToReturn();
    }


    protected final Document getXmlResult() throws IOException, SAXException {

        final String url = createRequestURL();
        LOG.info("Using " + url);
        return client.getXmlDocument(context.getSearchConfiguration().getName(), url);
    }

    protected final boolean isVgSiteSearch() {
        return context.getQuery().getQueryString().contains("site:vg.no");
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
