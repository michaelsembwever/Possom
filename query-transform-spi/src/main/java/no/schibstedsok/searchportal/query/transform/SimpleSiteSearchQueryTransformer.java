// Copyright (2006) Schibsted Søk AS
/*
 * SimpleSiteSearchQueryTransformer.java
 *
 * Created on January 25, 2006, 4:39 PM
 *
 */

package no.schibstedsok.searchportal.query.transform;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * SimpleSiteSearchQueryTransformer.
 * TODO remove this class once all sitesearches are using the new model.
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
 * @version <tt>$Id: SimpleSiteSearchQueryTransformer.java 4265 2007-01-04 13:54:03Z ssmiweve $</tt>
 * @deprecated Old style sitesearch. Use skins instead.
 */
public final class SimpleSiteSearchQueryTransformer extends AbstractQueryTransformer implements QueryTransformer {

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
    private String parameter;

    @Override
    public String getFilter(final Map parameters) {

        final String paramValue = parameters.get(parameter) instanceof String[]
                ? ((String[])parameters.get(parameter))[0]
                : (String)parameters.get(parameter);

        if (paramValue != null && paramValue.length() > 0) {
            if (!(paramValue.equals("") || paramValue.equals("d"))) {

                final Map<String,String> s = sites != null && sites.size()>0
                        ? new HashMap(DEFAULT_SITES)
                        : DEFAULT_SITES;
                if( s != DEFAULT_SITES ){
                    s.putAll(sites);
                }

                if (s.containsKey(paramValue)) {
                    return s.get(paramValue);
                }
            }
        } else {
            // return null if empty
            final String query = parameters.get("q") instanceof String[]
                ? ((String[])parameters.get("q"))[0]
                : (String)parameters.get("q");

            if (null == query || "".equals(query.trim()) ){
                return null;
            }

            // The site is given in the site parameter
            final String privateSite = parameters.get("sitesearch") instanceof String[]
                ? ((String[])parameters.get("sitesearch"))[0]
                : parameters.get("sitesearch") instanceof String ? (String)parameters.get("sitesearch") : null;
            
            if (privateSite != null && privateSite.length() > 0) {
                final String collection = parameters.get("c") instanceof String[]
                    ? ((String[])parameters.get("c"))[0]
                    : (String)parameters.get("c");

                // Also make sure that the c parameter is set to pss
                if (collection.equals("pss")) {
                    return "+site:" + privateSite;
                }
            }
        }
        return null;
    }
    
    public void setParameter(final String pn){
        parameter = pn;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final SimpleSiteSearchQueryTransformer retValue = (SimpleSiteSearchQueryTransformer)super.clone();
        retValue.parameter = parameter;
        retValue.sites = sites;
        return retValue;
    }
    
    @Override
    public QueryTransformer readQueryTransformer(final Element qt){
        
        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "parameter", ParseType.String, qt, "");
        return this;
    }
}
