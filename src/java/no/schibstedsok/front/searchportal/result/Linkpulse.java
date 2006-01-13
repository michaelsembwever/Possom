// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result;

import java.util.Properties;

/**
 * Linkpulse adds a part in front of the url for userlogging.
 * getUrl() is called from the templates by the links which should be logged.
 * Logging should only happen in prod.
 *
 * @author Thomas Kjærstad <a href="thomas@schibstedsok.no">thomas@schibstedsok.no</a>
 * @version $Id$
 */
public class Linkpulse {

    private String toUrl;
    private Properties props;

    public Linkpulse(final Properties properties) {
        this.props = properties;
    };

    public String  getUrl(final String orgUrl, final String paramString, final String script) {

        //linkpulse property is set to true only in the production build
        if (props.getProperty("tokenevaluator.linkpulse").equals("true")) {

            toUrl = props.getProperty("tokenevaluator.linkpulseToUrl") + script + "/";

            //click attributes comes as a string seperated by ';'
            String[] paramArr = paramString.split(";");
            for (int i = 0; i < paramArr.length; i++) {

                //the attributes is seperated by ';' in the url if it's more than one attribute
                if (i != 0) toUrl = toUrl + ";";

                //the attribute and the attribute value is seperated by ':'
                String[] attrArr = paramArr[i].split(":");
                for (int k = 1; k < attrArr.length; k++)
                    toUrl = toUrl + attrArr[0] + "=" + attrArr[1];

            }
            //adds the original url
            toUrl = toUrl + "/" + props.getProperty("tokenevaluator.linkpulseSesam") + orgUrl;
        } else
            toUrl = orgUrl;

        return toUrl;
    }

}
