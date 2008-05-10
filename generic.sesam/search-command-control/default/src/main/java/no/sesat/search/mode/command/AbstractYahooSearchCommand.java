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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import no.sesat.search.mode.config.AbstractYahooSearchConfiguration;
import no.sesat.search.site.config.SiteConfiguration;

import org.apache.log4j.Logger;

/**
 *
 *
 * @version $Id$
 */
public abstract class AbstractYahooSearchCommand extends AbstractXmlSearchCommand {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractYahooSearchCommand.class);

    // Attributes ----------------------------------------------------

    private final String partnerId;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new overture command.
     *
     * @param cxt The context to execute in.
     */
    public AbstractYahooSearchCommand(final Context cxt) {
        super(cxt);

        final AbstractYahooSearchConfiguration conf = (AbstractYahooSearchConfiguration)cxt.getSearchConfiguration();

        final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();

        partnerId = siteConf.getProperty(conf.getPartnerId());
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
    protected String getPartnerId(){
        return partnerId;
    }

    /**
     * @return Returns the affilData element for Yahoo/Overture, not including the prefix "&".
     */
    protected final String getAffilDataParameter() {
        final String remoteAddr = datamodel.getBrowser().getRemoteAddr().getString();
        final String forwardedFor = datamodel.getBrowser().getForwardedFor().getString();
        final String userAgent = datamodel.getBrowser().getUserAgent().getString();

        final StringBuilder affilDataValue = new StringBuilder();
        affilDataValue.append("ip=" + (remoteAddr != null ? remoteAddr : ""));
        affilDataValue.append("&ua=" + userAgent);

        if (forwardedFor != null && forwardedFor.trim().length() > 0) {
            affilDataValue.append("&xfip=" + forwardedFor);
        }

        try {
            return "affilData=" + URLEncoder.encode(affilDataValue.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should not happen...
            LOG.error(e);
            return null;
        }
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
