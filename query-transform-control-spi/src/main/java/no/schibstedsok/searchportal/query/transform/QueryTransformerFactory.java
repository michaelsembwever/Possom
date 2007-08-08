/*
 * QueryTransformerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.schibstedsok.searchportal.query.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.Site;

/** Obtain a working QueryTransformer from a given QueryTransformerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class QueryTransformerFactory {

    public interface Context extends SiteContext, BytecodeContext {
    }

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    private Context context;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    public QueryTransformerFactory(Context context) {
        this.context = context;
    }

    // Public --------------------------------------------------------

    /**
     *
     * @param config
     * @return
     */
    public QueryTransformer getController(final QueryTransformerConfig config){

        final String controllerName = "no.schibstedsok.searchportal.query.transform."
                + config.getClass().getAnnotation(Controller.class).value();

        try{
            final SiteClassLoaderFactory.Context ctrlCxt = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.QUERY_TRANSFORM_CONTROL;
                }
            };

            final SiteClassLoaderFactory.Context cfgCxt = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.QUERY_TRANSFORM_CONFIG;
                }
            };

            final ClassLoader ctrlClassLoader = SiteClassLoaderFactory.valueOf(ctrlCxt).getClassLoader();
            final ClassLoader cfgClassLoader = SiteClassLoaderFactory.valueOf(cfgCxt).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends QueryTransformer> cls
                    = (Class<? extends QueryTransformer>)ctrlClassLoader.loadClass(controllerName);

            // tricky tricky. took me some time to see this!
            //  one classLoader for the constructor, another for it's paramaeter! nice one magnus.
            @SuppressWarnings("unchecked")
            final Class<QueryTransformerConfig> cfgClass = (Class<QueryTransformerConfig>)
                    cfgClassLoader.loadClass(QueryTransformerConfig.class.getName());

            final Constructor<? extends QueryTransformer> constructor = cls.getConstructor(cfgClass);

            return constructor.newInstance(config);

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
