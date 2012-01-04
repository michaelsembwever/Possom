/*
 * Copyright (2007-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package no.sesat.search.run.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.mode.SearchModeFactory;
import no.sesat.search.run.transform.AbstractRunTransformerConfig.Controller;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;

/** Obtain a working RunTransformers.
 *
 *
 * $Id$
 */
public final class RunTransformerFactory {

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of RunTransformerFactory */
    private RunTransformerFactory() {
    }

    // Public --------------------------------------------------------


    /**
     *
     * @param config
     * @return
     */
    public static RunTransformer getController(
            final RunTransformer.Context context,
            final RunTransformerConfig config){

        final String name = "no.sesat.search.run.transform."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.RUN_TRANSFORM_CONFIG;
                        }
                    },
                    context
                );

            final ClassLoader ctlLoader = SiteClassLoaderFactory.instanceOf(ctlContext).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends RunTransformer> cls = (Class<? extends RunTransformer>)ctlLoader.loadClass(name);

            final Constructor<? extends RunTransformer> constructor = cls.getConstructor(RunTransformerConfig.class);

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
}
