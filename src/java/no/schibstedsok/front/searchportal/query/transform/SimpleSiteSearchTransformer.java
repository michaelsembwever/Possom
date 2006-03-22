// Copyright (2006) Schibsted Søk AS
/*
 * SimpleSiteSearchTransformer.java
 *
 * Created on January 25, 2006, 4:39 PM
 *
 */

package no.schibstedsok.front.searchportal.query.transform;

import java.util.Map;

/**
 * SimpleSiteSearchTransformer
 *
 * This transformer can be used in tabs.xml to create a simple site search.
 * It uses a map to find the actual string to use for the FAST site: filter
 *
 * e.g. ds => dinside.no, it => itavisen.no
 *
 * The key used when looking up is taken from request parameter
 * <code>parameterName</code>.
 *
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SimpleSiteSearchTransformer extends AbstractQueryTransformer implements QueryTransformer {

    private Map<String,String> sites;
    private String parameterName;
    private String filterName;

    public String getFilter(final Map parameters) {
      
        final String[] paramValue = (String[]) parameters.get(parameterName);

        if (paramValue != null && paramValue.length > 0) {
            if (!(paramValue[0].equals("") || paramValue[0].equals("d"))) {
                if (sites.containsKey(paramValue[0])) {
                    final StringBuffer filter = new StringBuffer("+");
                    filter.append(filterName);
                    filter.append(':');
                    filter.append(sites.get(paramValue[0]));
                    return filter.toString();
                }
            }
        }
        return null;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final SimpleSiteSearchTransformer retValue = (SimpleSiteSearchTransformer)super.clone();
        retValue.parameterName = parameterName;
        retValue.filterName = filterName;
        retValue.sites = sites;
        return retValue;
    }
}
