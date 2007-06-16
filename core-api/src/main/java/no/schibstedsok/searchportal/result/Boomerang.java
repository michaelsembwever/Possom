// Copyright (2005-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;

/**
 * Boomerang ensures outbound links are logged.
 *
 * <b> Immutable. </b>
 *
 * @author Thomas Kjærstad <a href="thomas@sesam.no">thomas@sesam.no</a>
 * @author Mck <a href="mick@sesam.no">mick@sesam.no</a>
 * @version $Id$
 */
public final class Boomerang {

    private static final Logger LOG = Logger.getLogger(Boomerang.class);

    private static final String ERR_TOO_MANY_COLONS = "Too many colons in key-value ";

    private static final String BASE_URL = "boomerang/";

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
        toUrl.append(orgUrl);

        return toUrl.toString();
    }

}
