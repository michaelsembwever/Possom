// Copyright (2006-2007) Schibsted Søk AS
/*
 * SimpleSiteSearchQueryTransformer.java
 *
 * Created on January 25, 2006, 4:39 PM
 *
 */

package no.schibstedsok.searchportal.query.transform;


import java.util.HashMap;
import java.util.Map;

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
public final class SimpleSiteSearchQueryTransformer extends AbstractQueryTransformer{

    private final SimpleSiteSearchQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public SimpleSiteSearchQueryTransformer(final QueryTransformerConfig config){
        this.config = (SimpleSiteSearchQueryTransformerConfig) config;
    }

    @Override
    public String getFilter(final Map parameters) {

        final String paramValue = parameters.get(config.getParameter()) instanceof String[]
                ? ((String[])parameters.get(config.getParameter()))[0]
                : (String)parameters.get(config.getParameter());

        if (paramValue != null && paramValue.length() > 0) {
            if (!(paramValue.equals("") || paramValue.equals("d"))) {

                final Map<String,String> s = config.getSites() != null && config.getSites().size()>0
                        ? new HashMap(config.getDefaultSites())
                        : config.getDefaultSites();
                if( s != config.getDefaultSites() ){
                    s.putAll(config.getSites());
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

}
