// Copyright (2006) Schibsted Søk AS
/*
 * SimpleSiteSearchTransformer.java
 *
 * Created on January 25, 2006, 4:39 PM
 *
 */

package no.schibstedsok.front.searchportal.query.transform;


import java.util.Collections;
import java.util.HashMap;
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
 * @version <tt>$Id$</tt>
 */
public final class SimpleSiteSearchTransformer extends AbstractQueryTransformer implements QueryTransformer {

    private static final Map<String,String> DEFAULT_SITES;

    static{
        final Map<String,String> defaultSites = new HashMap<String,String>();
        defaultSites.put("ds", "dinside.no");
        defaultSites.put("it", "itavisen.no");
        defaultSites.put("di", "digi.no");
        defaultSites.put("pr", "propaganda-as.no");
        defaultSites.put("im", "imarkedet.no");
        defaultSites.put("nrk", "nrk.no");
        defaultSites.put("af", "aftenposten.no");
        defaultSites.put("fv", "fedrelandsvennen.no");
        defaultSites.put("aa", "adressa.no");
        defaultSites.put("sa", "aftenbladet.no");
        defaultSites.put("bt", "bt.no");

        DEFAULT_SITES = Collections.unmodifiableMap(defaultSites);
    }

    private Map<String,String> sites;
    private String parameterName;
    private String filterName;

    public String getFilter(final Map parameters) {

        final String[] paramValue = (String[]) parameters.get(parameterName);

        if (paramValue != null && paramValue.length > 0) {
            if (!(paramValue[0].equals("") || paramValue[0].equals("d"))) {

                final Map<String,String> s = sites != null && sites.size()>0
                        ? new HashMap(DEFAULT_SITES)
                        : DEFAULT_SITES;
                if( s != DEFAULT_SITES ){
                    s.putAll(sites);
                }

                if (s.containsKey(paramValue[0])) {
                    final StringBuilder filter = new StringBuilder("+");
                    filter.append(filterName);
                    filter.append(':');
                    filter.append(s.get(paramValue[0]));
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
