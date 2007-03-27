// Copyright (2006-2007) Schibsted Søk AS
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
import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
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
 * @version <tt>$Id$</tt>
 * @deprecated Old style sitesearch. Use skins instead.
 */
@Controller("SimpleSiteSearchQueryTransformer")
public final class SimpleSiteSearchQueryTransformerConfig extends AbstractQueryTransformerConfig {

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

    /**
     *
     * @param pn
     */
    public void setParameter(final String pn){
        parameter = pn;
    }

    /**
     *
     * @return
     */
    public Map<String,String> getSites(){
        return sites;
    }

    /**
     *
     * @return
     */
    public Map<String,String> getDefaultSites(){
        return DEFAULT_SITES;
    }

    /**
     *
     * @return
     */
    public String getParameter(){
        return parameter;
    }

    @Override
    public SimpleSiteSearchQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "parameter", ParseType.String, qt, "");
        return this;
    }
}
