/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.transform;

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



    public String getFilter(final Context cxt) {
        final Pattern sitePattern = Pattern.compile(sourcePrefix + ":([^\\s]*)");
        final Matcher m = sitePattern.matcher(cxt.getTransformedQuery());
        if (m.find()) {
            return "+" + targetPrefix + ":" + m.group(1);
        } else {
            return null;
        }
    }
}
