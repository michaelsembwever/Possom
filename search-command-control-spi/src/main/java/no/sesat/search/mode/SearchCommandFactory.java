/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.sesat.search.mode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;

import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeContext;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;


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

        final String controllerName = "no.sesat.search.mode.command."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context classContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.SEARCH_COMMAND_CONTROL;
                        }
                    },
                    context
                );

            final SiteClassLoaderFactory loaderFactory = SiteClassLoaderFactory.valueOf(classContext);

            @SuppressWarnings("unchecked")
            final Class<? extends SearchCommand> cls
                    = (Class<? extends SearchCommand>) loaderFactory.getClassLoader().loadClass(controllerName);

            final Constructor<? extends SearchCommand> constructor = cls.getConstructor(SearchCommand.Context.class);

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
