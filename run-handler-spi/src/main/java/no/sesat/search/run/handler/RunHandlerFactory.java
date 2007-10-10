/*
 * Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ResultHandlerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.search.run.handler;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;

/** Obtain a working RunningQueryHandler.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class RunHandlerFactory {

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    private RunHandlerFactory() {
    }

    // Public --------------------------------------------------------


    /**
     *
     * @param config
     * @return
     */
/*    public static RunningQueryHandler getController(
            final RunningQueryHandler.Context context){

        final String name = "no.sesat.search.result.handler."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.RUN_HANDLER;
                        }
                    },
                    context
                );

            final ClassLoader ctlLoader = SiteClassLoaderFactory.valueOf(ctlContext).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends ResultHandler> cls = (Class<? extends ResultHandler>)ctlLoader.loadClass(name);

            final Constructor<? extends ResultHandler> constructor = cls.getConstructor(ResultHandlerConfig.class);

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
    }*/

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
