/* Copyright (2005-2006) Schibsted Søk AS
 *
 * VelocityEngineFactory.java
 *
 * Created on 3 February 2006, 13:24
 *
 */

package no.schibstedsok.front.searchportal.view.velocity;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import no.schibstedsok.front.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class VelocityEngineFactory implements SiteKeyedFactory{

    private static final Logger LOG = Logger.getLogger(VelocityEngineFactory.class);
    private static final String VELOCITY_LOGGER = "org.apache.velocity";

    private static final Map<Site,VelocityEngineFactory> INSTANCES = new HashMap<Site,VelocityEngineFactory>();
    
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private final VelocityEngine engine;

    /**
     * The context the AnalysisRules must work against. *
     */
    public interface Context extends SiteContext, ResourceContext {
    }

    private final Context context;

    /** Creates a new instance of VelocityEngineFactory */
    private VelocityEngineFactory(final Context cxt) {
        
        INSTANCES_LOCK.writeLock().lock();
        context = cxt;
        final Site site = cxt.getSite();

        engine = new VelocityEngine(){
            /** We override this method to dampen the <ERROR velocity: ResourceManager : unable to find resource ...>
             * error messages in sesam.error
             **/
            public Template getTemplate(final String name) throws ResourceNotFoundException, ParseErrorException, Exception {

                final Level level = Logger.getLogger(VELOCITY_LOGGER).getLevel();
                Logger.getLogger(VELOCITY_LOGGER).setLevel(Level.FATAL);

                final Template retValue = super.getTemplate(name);

                Logger.getLogger(VELOCITY_LOGGER).setLevel(level);
                return retValue;
            }

        };

        try  {
            final Logger logger = Logger.getLogger(VELOCITY_LOGGER);
            final Properties props = SiteConfiguration.valueOf(
                    ContextWrapper.wrap(SiteConfiguration.Context.class, cxt)).getProperties();
            // engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute"); // velocity 1.5
            engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            // engine.setProperty("runtime.log.logsystem.log4j.logger", logger.getName()); // velocity 1.5
            engine.setProperty("runtime.log.logsystem.log4j.category", logger.getName());
            engine.setProperty(Velocity.RESOURCE_LOADER, "url");
            engine.setProperty("url.resource.loader.class", "no.schibstedsok.front.searchportal.view.velocity.URLVelocityTemplateLoader");
            engine.setProperty("url.resource.loader.cache", "true");
            engine.setProperty("url.resource.loader.modificationCheckInterval", "300"); // 5 minute update cycle.
            engine.setProperty("velocimacro.library", site.getTemplateDir() + "/VM_global_library.vm");
            engine.setProperty(Site.NAME_KEY, site);
            engine.setProperty("site.fallback", Site.DEFAULT);
            engine.setProperty(SearchConstants.PUBLISH_SYSTEM_URL, props.getProperty(SearchConstants.PUBLISH_SYSTEM_URL));
            engine.setProperty(SearchConstants.PUBLISH_SYSTEM_HOST, props.getProperty(SearchConstants.PUBLISH_SYSTEM_HOST));
            engine.setProperty("input.encoding", "UTF-8");
            engine.setProperty("output.encoding", "UTF-8");
            engine.setProperty("userdirective", "no.schibstedsok.front.searchportal.view.velocity.UrlEncodeDirective,no.schibstedsok.front.searchportal.view.velocity.HtmlEscapeDirective,no.schibstedsok.front.searchportal.view.velocity.CapitalizeWordsDirective,no.schibstedsok.front.searchportal.view.velocity.ChopStringDirective,no.schibstedsok.front.searchportal.view.velocity.PublishDirective,no.schibstedsok.front.searchportal.view.velocity.AccountingDirective,no.schibstedsok.front.searchportal.view.velocity.RolesDirective,no.schibstedsok.front.searchportal.view.velocity.XmlEscapeDirective,no.schibstedsok.front.searchportal.view.velocity.WikiDirective");
            engine.init();

        } catch (Exception e) {
            throw new InfrastructureException(e);
        }

        
        INSTANCES.put(site, this);
        INSTANCES_LOCK.writeLock().unlock();
    }

    public VelocityEngine getEngine() {
        return engine;
    }

    /** Main method to retrieve the correct VelocityEngine to further obtain
     * AnalysisRule.
     * @param cxt the contextual needs the VelocityEngine must use to operate.
     * @return VelocityEngine for this site.
     */
    public static VelocityEngineFactory valueOf(final Context cxt) {
        
        final Site site = cxt.getSite();
        INSTANCES_LOCK.readLock().lock();
        VelocityEngineFactory instance = INSTANCES.get(site);
        INSTANCES_LOCK.readLock().unlock();
        
        
        if (instance == null) {
            instance = new VelocityEngineFactory(cxt);

        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load all resources.</b>
     * @param site the site the VelocityEngine will work for.
     * @return VelocityEngine for this site.
     */
    public static VelocityEngineFactory valueOf(final Site site) {

        // RegExpEvaluatorFactory.Context for this site & UrlResourceLoader.
        final VelocityEngineFactory instance = VelocityEngineFactory.valueOf(new VelocityEngineFactory.Context() {
            public Site getSite() {
                return site;
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }
        });
        return instance;
    }

    public boolean remove(Site site) {
        
        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

}
