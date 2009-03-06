/* Copyright (2005-2009) Schibsted Søk AS
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
package no.sesat.search.result;

import java.net.MalformedURLException;
import java.util.StringTokenizer;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteConfiguration;
import org.apache.log4j.Logger;

/**
 * Boomerang manipulates URL links, for example to ensure outbound links are logged.
 *
 * <b> Immutable. </b>
 *
 *
 *
 * @version $Id$
 */
public final class Boomerang {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(Boomerang.class);

    private static final String BASE_URL = "boomerang/";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    public static String getUrl(
            final Site site,
            final String orgUrl,
            final String paramString) {

        final StringBuilder toUrl = new StringBuilder("http://" + site.getName() + BASE_URL);

        // click attributes comes as a string seperated by ';'
        final StringTokenizer tokeniser = new StringTokenizer(paramString, ";");
        while(tokeniser.hasMoreTokens()){
            toUrl.append(tokeniser.nextToken().replace(':', '='));
            toUrl.append(';');
        }

        // remove last ';'
        toUrl.setLength(toUrl.length() - 1);

        toUrl.append('/');

        // add the destination url
        if(!orgUrl.startsWith("http")){

            // any relative url must be made absolute against the current skin
            toUrl.append("http://" + site.getName());

            // avoid double /
            if(orgUrl.startsWith("/")){
                toUrl.setLength(toUrl.length() - 1);
            }
        }

        // append the original destination url
        toUrl.append(toIDN(orgUrl));

        return toUrl.toString();
    }

    private static String toIDN(String url) {
        String host = null;
        try {
            host = new java.net.URL(url).getHost();
        } catch (MalformedURLException ex) {
            // LOG.warn("Invalid url in boomerang: " + url, ex);
        }

        if (host != null) {
            url = url.replace(host, java.net.IDN.toASCII(host));
        }
        return url;
    }

    // Constructors -------------------------------------------------

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
