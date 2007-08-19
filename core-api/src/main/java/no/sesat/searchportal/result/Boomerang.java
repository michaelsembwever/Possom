/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
*/
package no.sesat.searchportal.result;

import java.util.StringTokenizer;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.config.SiteConfiguration;
import org.apache.log4j.Logger;

/**
 * Boomerang manipulates URL links, for example to ensure outbound links are logged.
 *
 * <b> Immutable. </b>
 *
 * @author Thomas Kjærstad <a href="thomas@sesam.no">thomas@sesam.no</a>
 * @author Mck <a href="mick@sesam.no">mick@sesam.no</a>
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
        toUrl.append(orgUrl);

        return toUrl.toString();
    }

    // Constructors -------------------------------------------------
    
    // Public --------------------------------------------------------
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
