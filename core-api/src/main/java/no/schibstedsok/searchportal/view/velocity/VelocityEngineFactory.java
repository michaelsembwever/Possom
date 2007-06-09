/* Copyright (2005-2007) Schibsted Søk AS
 *
 * VelocityEngineFactory.java
 *
 * Created on 3 February 2006, 13:24
 *
 */

package no.schibstedsok.searchportal.view.velocity;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import no.geodata.maputil.CoordHelper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.result.Decoder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;

/** Custom Factory around Velocity Engines and Templates.
 * Each instance maps to an VelocityEngine instance.
 * All template operations (getting and merging) are done through this class
 *   rather than directly against Velocity's API.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class VelocityEngineFactory implements SiteKeyedFactory{

    /**
     * The context the AnalysisRules must work against.
     */
    public interface Context extends SiteContext, ResourceContext {
    }

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(VelocityEngineFactory.class);

    private static final String INFO_TEMPLATE_NOT_FOUND = "Could not find template ";
    private static final String ERR_IN_TEMPLATE = "Error parsing template ";
    private static final String ERR_GETTING_TEMPLATE = "Error getting template ";

    private static final String VELOCITY_LOGGER = "org.apache.velocity";

    private static final Map<Site,VelocityEngineFactory> INSTANCES = new HashMap<Site,VelocityEngineFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final String LOGSYSTEM_CLASS = "org.apache.velocity.runtime.log.Log4JLogChute";
    private static final String LOG_NAME = "runtime.log.logsystem.log4j.logger";

    private static final boolean VELOCITY_DEBUG = Boolean.getBoolean("VELOCITY_DEBUG");

    private static final String DIRECTIVES =
            "no.schibstedsok.searchportal.view.velocity.UrlEncodeDirective,"
            + "no.schibstedsok.searchportal.view.velocity.HtmlEscapeDirective,"
            + "no.schibstedsok.searchportal.view.velocity.CapitalizeWordsDirective,"
            + "no.schibstedsok.searchportal.view.velocity.ChopStringDirective,"
            + "no.schibstedsok.searchportal.view.velocity.PublishDirective,"
            + "no.schibstedsok.searchportal.view.velocity.AccountingDirective,"
            + "no.schibstedsok.searchportal.view.velocity.RolesDirective,"
            + "no.schibstedsok.searchportal.view.velocity.ShareHoldersDirective,"
            + "no.schibstedsok.searchportal.view.velocity.RolesMobilePeopleExportDirective,"
            + "no.schibstedsok.searchportal.view.velocity.XmlEscapeDirective,"
            + "no.schibstedsok.searchportal.view.velocity.MailEncodeDirective,"
            + "no.schibstedsok.searchportal.view.velocity.WikiDirective,"
            + "no.schibstedsok.searchportal.view.velocity.UpperCaseDirective,"
            + "no.schibstedsok.searchportal.view.velocity.WeekdayDirective,"
            + "no.schibstedsok.searchportal.view.velocity.MD5ParameterDirective,"
            + "no.schibstedsok.searchportal.view.velocity.FinnImgLinkDirective,"
            + "no.schibstedsok.searchportal.view.velocity.TopDomainDirective,"
            + "no.schibstedsok.searchportal.view.velocity.DateFormattingDirective,"
            + "no.schibstedsok.searchportal.view.velocity.BoldWordDirective,"
            + "no.schibstedsok.searchportal.view.velocity.ChannelCategoryListDirective,"
            + "no.schibstedsok.searchportal.view.velocity.RemovePrefixDirective,"
            + "no.schibstedsok.searchportal.view.velocity.NavigatorDirective,"
            + "no.schibstedsok.searchportal.view.velocity.SlashTrimStringDirective";


    // Attributes ----------------------------------------------------

    private final VelocityEngine engine;





    // Static --------------------------------------------------------


    public static Template getTemplate(
            final VelocityEngine engine,
            final Site site,
            final String templateName) throws ResourceNotFoundException{

        final String templateUrl = site.getTemplateDir() + "/" + templateName + ".vm";

        try {
            return engine.getTemplate(templateUrl);


        } catch (ParseErrorException ex) {
            LOG.error(ERR_IN_TEMPLATE + templateUrl, ex);
            throw new InfrastructureException(ex);

        } catch (ResourceNotFoundException ex) {
            // throw this so callers know we did not find the resource.
            throw ex;

        } catch (Exception ex) {
            LOG.error(ERR_GETTING_TEMPLATE + templateUrl, ex);
            throw new InfrastructureException(ex);
        }
    }

    public static VelocityContext newContextInstance(final VelocityEngine engine){

        final VelocityContext context = new VelocityContext();

        // coord helper
        context.put("coordHelper", new CoordHelper());
        // decoder
        context.put("decoder", new Decoder());
        // math tool
        context.put("math", new MathTool());
        // date tool
        context.put("date", new DateTool());

        return context;
    }


    /** Main method to retrieve the correct VelocityEngine to further obtain
     * AnalysisRule.
     * @param cxt the contextual needs the VelocityEngine must use to operate.
     * @return VelocityEngine for this site.
     */
    public static VelocityEngineFactory valueOf(final Context cxt) {

        final Site site = cxt.getSite();

        VelocityEngineFactory instance = null;

        try {
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        } finally {
            INSTANCES_LOCK.readLock().unlock();
        }

        if(!VELOCITY_DEBUG) {
            if (instance == null) {
                instance = new VelocityEngineFactory(cxt);
            }
        } else {
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
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {

                return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public DocumentLoader newDocumentLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final DocumentBuilder builder) {

                return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }
            public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                return UrlResourceLoader.newBytecodeLoader(site, name, jar);
            }

        });
        return instance;
    }


    // Constructors --------------------------------------------------

    /** Creates a new instance of VelocityEngineFactory */
    private VelocityEngineFactory(final Context cxt) {


        try{
            INSTANCES_LOCK.writeLock().lock();
            final Site site = cxt.getSite();
            final Logger logger = Logger.getLogger(VELOCITY_LOGGER);

            engine = new VelocityEngine(){
                /** We override this method to dampen the
                 * <ERROR velocity: ResourceManager : unable to find resource ...>
                 * error messages in sesam.error
                 **/
                public Template getTemplate(final String name)
                        throws ResourceNotFoundException, ParseErrorException, Exception {

                    final Level level = logger.getLevel();
                    logger.setLevel(Level.FATAL);

                    final Template retValue = super.getTemplate(name);

                    logger.setLevel(level);
                    return retValue;
                }

            };

            try  {

                engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, LOGSYSTEM_CLASS);
                engine.setProperty(LOG_NAME, logger.getName());
                engine.setProperty(Velocity.RESOURCE_LOADER, "url");
                engine.setProperty("url.resource.loader.class", URLVelocityTemplateLoader.class.getName());

                if(VELOCITY_DEBUG) {
                    engine.setProperty("url.resource.loader.cache", "false");
                    engine.setProperty("velocimacro.library.autoreload", "true");                    
                } else {
                	engine.setProperty("url.resource.loader.cache", "true");
                	engine.setProperty("url.resource.loader.modificationCheckInterval", "60");
                    engine.setProperty(Velocity.RESOURCE_MANAGER_CLASS, QuickResourceManagerImpl.class.getName());
                    engine.setProperty(Velocity.RESOURCE_MANAGER_CACHE_CLASS, QuickResourceCacheImpl.class.getName());
                    engine.setProperty(Velocity.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, "0");

                    // Use custom unbound quicker resource cache.
                    engine.setProperty(QuickResourceCacheImpl.INITIAL_SIZE_PROPERTY, 1000);
                }

                engine.setProperty(Site.NAME_KEY, site);

                engine.setProperty("input.encoding", "UTF-8");
                engine.setProperty("output.encoding", "UTF-8");
                engine.setProperty("userdirective", DIRECTIVES);
                engine.setProperty(
                        "velocimacro.library",
                        site.getTemplateDir() + "/VM_global_library.vm,"
                        + site.getTemplateDir() + "/VM_site_library.vm,"
                        + site.getTemplateDir() + "/VM_map_library.vm");
                engine.init();

            } catch (Exception e) {
                throw new InfrastructureException(e);
            }


            INSTANCES.put(site, this);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Public --------------------------------------------------------

    public VelocityEngine getEngine() {
        return engine;
    }

    public boolean remove(final Site site) {

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------


    // Inner classes -------------------------------------------------
}
