// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Properties;
import no.schibstedsok.searchportal.site.Site;

/**
 * Linkpulse adds a part in front of the url for userlogging.
 * getUrl() is called from the templates by the links which should be logged.
 * Logging should only happen in prod.
 *
 * @author Thomas Kjærstad <a href="thomas@schibstedsok.no">thomas@schibstedsok.no</a>
 * @version $Id$
 */
public final class Linkpulse {
    
    private static final String ERR_TOO_MANY_COLONS = "Too many colons in key-value ";

    private final Site site;
    private final Properties props;

    public Linkpulse(
            final Site site,
            final Properties properties) {
        
        this.site = site;
        this.props = properties;
    }

    public String  getUrl(
            final String orgUrl, 
            final String paramString, 
            final String script, 
            final String indexpage) {

        final StringBuilder toUrl = new StringBuilder();

        //linkpulse property is set to true only in the production build
        if (Boolean.valueOf(props.getProperty("linkpulse.enable"))) {

            toUrl.append(props.getProperty("linkpulse.url") + script + '/');

            //click attributes comes as a string seperated by ';'
            final String[] paramArr = paramString.split(";");
            for (int i = 0; i < paramArr.length; i++) {

                //the attributes is seperated by ';' in the url if it's more than one attribute
                if (i != 0){
                    toUrl.append(';');
                }

                //the attribute and the attribute value is seperated by ':'
                final String[] attrArr = paramArr[i].split(":");
                switch(attrArr.length){
                    case 1:
                        toUrl.append(attrArr[0]);
                        break;
                    case 2:
                        toUrl.append(attrArr[0] + '=' + attrArr[1]);
                        break;
                    default:
                        throw new IllegalArgumentException(ERR_TOO_MANY_COLONS + paramArr[i]);
                }
            }
            //adds to-url, if to-url links to external site we must drop site name prefix         
            toUrl.append( indexpage.equalsIgnoreCase("ext")
                    ?  '/' + orgUrl
                    :  "/http://" + site.getName() + "search/" + orgUrl);
        } else{
            
           toUrl.append( Boolean.valueOf(indexpage)
                   ? "search/" + orgUrl
                   : orgUrl);
        }

        return toUrl.toString();
    }

}
