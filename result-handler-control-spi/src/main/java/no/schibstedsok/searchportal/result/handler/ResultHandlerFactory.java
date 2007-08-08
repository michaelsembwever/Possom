/*
 * ResultHandlerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.schibstedsok.searchportal.result.handler;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.config.SiteClassLoaderFactory;
import no.schibstedsok.searchportal.site.config.Spi;

/** Obtain a working ResultHandler from a given ResultHandlerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ResultHandlerFactory {

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    private ResultHandlerFactory() {
    }

    // Public --------------------------------------------------------


    /**
     *
     * @param config
     * @return
     */
    public static ResultHandler getController(
            final ResultHandler.Context context,
            final ResultHandlerConfig config){

        final String name = "no.schibstedsok.searchportal.result.handler."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.RESULT_HANDLER_CONTROL;
                        }
                    },
                    context
                );

            final SiteClassLoaderFactory.Context cfgContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.RESULT_HANDLER_CONFIG;
                        }
                    },
                    context
                );

            final ClassLoader ctlLoader = SiteClassLoaderFactory.valueOf(ctlContext).getClassLoader();
            final ClassLoader cfgLoader = SiteClassLoaderFactory.valueOf(cfgContext).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends ResultHandler> cls = (Class<? extends ResultHandler>)ctlLoader.loadClass(name);

            // one classLoader for the constructor, another for it's paramaeter!
            @SuppressWarnings("unchecked")
            final Class<ResultHandlerConfig> cfgClass = (Class<ResultHandlerConfig>)
                    cfgLoader.loadClass(ResultHandlerConfig.class.getName());

            final Constructor<? extends ResultHandler> constructor = cls.getConstructor(cfgClass);

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
