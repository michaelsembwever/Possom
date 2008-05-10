/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.view;

import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;
import no.sesat.search.view.navigation.NavigationConfig;
import no.sesat.search.view.navigation.NavigationControllerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 *
 *
 * @version $Id$
 */
public final class NavigationControllerSpiFactory {

    public interface Context extends SiteContext, BytecodeContext {}

    private final Context context;

    /**
     *
     * @param context
     */
    public NavigationControllerSpiFactory(final Context context) {
        this.context = context;
    }

    public <T extends NavigationConfig.Nav> NavigationControllerFactory<T> getController(final T navConf){

        final String controllerName = navConf.getClass().getAnnotation(NavigationConfig.Nav.ControllerFactory.class).value();

        try{

            final SiteClassLoaderFactory.Context classContext = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.VIEW_CONTROL;
                }
            };

            final SiteClassLoaderFactory loaderFactory = SiteClassLoaderFactory.instanceOf(classContext);

            @SuppressWarnings("unchecked")
            final Class<? extends NavigationControllerFactory<T>> factory
                    = (Class<? extends NavigationControllerFactory<T>>)
                    loaderFactory.getClassLoader().loadClass(controllerName);

            final Constructor<? extends NavigationControllerFactory<T>> constructor
                    = (Constructor<? extends NavigationControllerFactory<T>>) factory.getConstructor();

            return constructor.newInstance();

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