/* Copyright (2005-2006) Schibsted Søk AS
 *
 * VelocityEngineFactory.java
 *
 * Created on 3 February 2006, 13:24
 *
 */

package no.schibstedsok.front.searchportal.result.handler.velocity;

import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class VelocityEngineFactory {
    
    private static final Logger LOG = Logger.getLogger(VelocityEngineFactory.class);
    private static final String VELOCITY_LOG_CATEGORY = "org.apache.velocity";
    
    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map/*<Site,VelocityEngine>*/ INSTANCES = new HashMap/*<Site,VelocityEngine>*/();

    private final VelocityEngine engine;
    
    /**
     * The context the AnalysisRules must work against. *
     */
    public interface Context extends SiteContext {
    }
    
    private final Context context;
    
    /** Creates a new instance of VelocityEngineFactory */
    private VelocityEngineFactory(final Context cxt) {
        context = cxt;
        final Site site = cxt.getSite();
        
        engine = new VelocityEngine();
        
        try{
            final Logger logger = Logger.getLogger(VELOCITY_LOG_CATEGORY);
            engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            engine.setProperty("runtime.log.logsystem.log4j.category", logger.getName());
            engine.setProperty(Velocity.RESOURCE_LOADER, "url");
            engine.setProperty("url.resource.loader.class", "no.schibstedsok.front.searchportal.result.handler.velocity.URLVelocityTemplateLoader");
            engine.setProperty("url.resource.loader.cache", "true");
            engine.setProperty("url.resource.loader.modificationCheckInterval", "300"); // 5 minute update cycle.
            engine.setProperty("velocimacro.library", site.getTemplateDir()+"/VM_global_library.vm");
            engine.setProperty("url.site", site);
            engine.setProperty("url.site.fallback",Site.DEFAULT);
            engine.init();
            
        } catch (Exception e) {
            throw new InfrastructureException(e);
        }
        
        INSTANCES.put(site,this);
    }
    
    public VelocityEngine getVelocityEngine(){
        return engine;
    }
    
    /** Main method to retrieve the correct VelocityEngine to further obtain
     * AnalysisRule.
     * @param cxt the contextual needs the VelocityEngine must use to operate.
     * @return VelocityEngine for this site.
     */
    public static VelocityEngine valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        VelocityEngineFactory instance = (VelocityEngineFactory) INSTANCES.get(site);
        if ( instance == null ) {
                instance = new VelocityEngineFactory(cxt);

        }
        return instance.getVelocityEngine();
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load all resources.</b>
     * @param site the site the VelocityEngine will work for.
     * @return VelocityEngine for this site.
     */
    public static VelocityEngine valueOf(final Site site) {

        // RegExpEvaluatorFactory.Context for this site & UrlResourceLoader.
        final VelocityEngine instance = VelocityEngineFactory.valueOf(new VelocityEngineFactory.Context() {
            public Site getSite() {
                return site;
            }

        });
        return instance;
    }    
    
}
