// Copyright (2006-2007) Schibsted Søk AS
/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.schibstedsok.searchportal.mode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.Site;


/** This factory creates the appropriate command for a given SearchConfiguration.
 *
 * @author mick
 * @version $Id: SearchCommandFactory.java 3359 2006-08-03 08:13:22Z mickw $
 */
public final class SearchCommandFactory {

    public interface Context extends SiteContext, BytecodeContext {}

    private final Context context;

    /**
     *
     * @param context
     */
    public SearchCommandFactory(final Context context) {
        this.context = context;
    }


    /** Create the appropriate command given the configuration inside the context.
     *
     * @param cxt
     * @return
     */
    public SearchCommand getController(final SearchCommand.Context cxt){

        final SearchConfiguration config = cxt.getSearchConfiguration();

        final String controllerName = "no.schibstedsok.searchportal.mode.command."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context classContext = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }

                public Site getSite() {
                    return context.getSite();
                }

                public Spi getSpi() {
                    return Spi.SEARCH_COMMAND_CONTROL;
                }
            };

            final SiteClassLoaderFactory loaderFactory = SiteClassLoaderFactory.valueOf(classContext);

            final Class<? extends SearchCommand> cls
                    = (Class<? extends SearchCommand>) loaderFactory.getClassLoader().loadClass(controllerName);

            final Constructor<? extends SearchCommand> constructor
                    = cls.getConstructor(SearchCommand.Context.class);

            return constructor.newInstance(cxt);

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
}
