// Copyright (2006-2007) Schibsted Søk AS
/*
 * AbstractYahooSearchCommand.java
 *
 * Created on June 12, 2006, 10:51 AM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.util.Map;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.AbstractYahooSearchConfiguration;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
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

    private final HTTPClient client;
    private final String partnerId;

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
            final DataModel datamodel) {

        super(cxt, datamodel);

        final AbstractYahooSearchConfiguration conf = (AbstractYahooSearchConfiguration)cxt.getSearchConfiguration();

        final SiteConfiguration siteConf
                = SiteConfiguration.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));
        final String host = siteConf.getProperty(conf.getHost());
        final int port = Integer.parseInt(siteConf.getProperty(conf.getPort()));


        client = null != conf.getHostHeader() && conf.getHostHeader().length() >0
                ? HTTPClient.instance(conf.getName(), host, port, conf.getHostHeader())
                : HTTPClient.instance(conf.getName(), host, port);

        partnerId = siteConf.getProperty(conf.getPartnerId());
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

    protected String getPartnerId(){
        return partnerId;
    }

    protected final Document getXmlResult() throws IOException, SAXException {

        final String url = createRequestURL();
        LOG.info("Using " + url);
        return client.getXmlDocument(context.getSearchConfiguration().getName(), url);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
