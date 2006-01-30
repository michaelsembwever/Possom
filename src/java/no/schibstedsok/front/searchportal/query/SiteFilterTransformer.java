/*
 * Copyright (2005) Schibsted S�k AS
 */
package no.schibstedsok.front.searchportal.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SiteFilterTransformer.
 * 
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class SiteFilterTransformer extends AbstractQueryTransformer implements QueryTransformer {

    private String sourcePrefix;
    private String targetPrefix;
    


    public String getFilter(Context cxt) {
        Pattern sitePattern = Pattern.compile(sourcePrefix + ":([^\\s]*)");
        Matcher m = sitePattern.matcher(cxt.getQueryString());
        if (m.find()) {
            return "+" + targetPrefix + ":" + m.group(1);
        } else {
            return null;
        }
    }
}
