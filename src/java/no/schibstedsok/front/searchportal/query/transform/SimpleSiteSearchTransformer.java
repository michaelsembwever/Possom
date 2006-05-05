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

        defaultSites.put("ds", "+site:dinside.no");
        defaultSites.put("it", "+site:itavisen.no");
        defaultSites.put("di", "+site:digi.no");
        defaultSites.put("pr", "+site:propaganda-as.no");
        defaultSites.put("im", "+site:imarkedet.no");
        defaultSites.put("nrk", "+site:nrk.no");
        defaultSites.put("af", "+site:(aftenposten.no forbruker.no)");
        defaultSites.put("fv", "+site:(fedrelandsvennen.no fvn.no)");
        defaultSites.put("aa", "+site:adressa.no");
        defaultSites.put("sa", "+site:aftenbladet.no");
        defaultSites.put("bt", "+site:bt.no");

        DEFAULT_SITES = Collections.unmodifiableMap(defaultSites);
    }

    private Map<String,String> sites;
    private String parameterName;

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
                    return s.get(paramValue[0]);
                }
            }
        } else {
            // return null if empty
            final String[] query = (String[]) parameters.get("q");
            if (query[0].trim().equals(""))
                return null;

            // The site is given in the psite parameter
            final String[] privateSite = (String[]) parameters.get("psite");

            if (privateSite != null && privateSite.length > 0) {
                final String[] def = (String[]) parameters.get("s");
//                final String[] track = (String []) parameters.get("ss_lt");
                
                // Also make sure that the statistics parameter is present
                if ((def == null || def.length == 0 || def[0].equals("pss"))) {
                    return "+site:" + privateSite[0];
                }
            }
        }
        return null;
    }
    
    public void setParameterName(final String pn){
        parameterName = pn;
    }

    public Object clone() throws CloneNotSupportedException {
        final SimpleSiteSearchTransformer retValue = (SimpleSiteSearchTransformer)super.clone();
        retValue.parameterName = parameterName;
        retValue.sites = sites;
        return retValue;
    }
}
